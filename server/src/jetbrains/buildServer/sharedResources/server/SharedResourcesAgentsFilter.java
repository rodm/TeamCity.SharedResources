package jetbrains.buildServer.sharedResources.server;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import jetbrains.buildServer.sharedResources.model.Lock;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static jetbrains.buildServer.sharedResources.server.SharedResourcesUtils.extractLocksFromPromotion;
import static jetbrains.buildServer.sharedResources.server.SharedResourcesUtils.getBuildPromotions;

/**
 * Class {@code SharedResourcesAgentsFilter}
 * <p/>
 * Filters all agents if some required locks are unavailable
 *
 * @author Oleg Rybak
 */
public class SharedResourcesAgentsFilter implements StartingBuildAgentsFilter {

  private static final Logger LOG = Logger.getInstance(StartingBuildAgentsFilter.class.getName());

  @NotNull
  @Override
  public AgentsFilterResult filterAgents(@NotNull AgentsFilterContext context) {
    final AgentsFilterResult result = new AgentsFilterResult();
    final QueuedBuildInfo queuedBuild = context.getStartingBuild();
    final Collection<Lock> locksToTake = extractLocksFromPromotion(queuedBuild.getBuildPromotionInfo());
    if (!locksToTake.isEmpty()) { // if our build has some locks
      final Collection<RunningBuildInfo> running = context.getDistributorInput().getRunningBuilds();
      final Collection<QueuedBuildInfo> distributed = context.getDistributedBuilds().keySet();
      final Collection<BuildPromotionInfo> buildPromotions = getBuildPromotions(running, distributed);
      // locks that are needed, but occupied
      final Collection<Lock> unavailableLocks = SharedResourcesUtils.getUnavailableLocks(locksToTake, buildPromotions);
      if (!unavailableLocks.isEmpty()) {
        final StringBuilder builder = new StringBuilder("Build is waiting for lock(s): \n");
        for (Lock lock : unavailableLocks) {
          builder.append(lock.getName()).append(", ");
        }
        final String reasonDescription = builder.substring(0, builder.length() - 2);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Got wait reason: [" + reasonDescription + "]");
        }
        result.setWaitReason(new SimpleWaitReason(reasonDescription));
      }
    }
    return result;
  }
}
