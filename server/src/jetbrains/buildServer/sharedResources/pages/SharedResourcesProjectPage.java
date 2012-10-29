package jetbrains.buildServer.sharedResources.pages;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.sharedResources.settings.SharedResourcesTabProjectSettings;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.project.ProjectTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static jetbrains.buildServer.sharedResources.SharedResourcesPluginConstants.SERVICE_NAME;

/**
 * Created with IntelliJ IDEA.
 * Date: 24.10.12
 * Time: 13:43
 *
 * @author Oleg Rybak
 */
public class SharedResourcesProjectPage extends ProjectTab {

  private static final Logger LOG = Logger.getInstance(SharedResourcesProjectPage.class.getName());

  private final WebControllerManager myWebControllerManager;
  private ProjectSettingsManager myProjectSettingsManager;

  protected SharedResourcesProjectPage(@NotNull PagePlaces pagePlaces,
                                       @NotNull ProjectManager projectManager,
                                       @NotNull WebControllerManager webControllerManager,
                                       @NotNull PluginDescriptor descriptor,
                                       @NotNull ProjectSettingsManager projectSettingsManager
  ) {
    super("sharedResources", "Shared Resources", pagePlaces, projectManager, descriptor.getPluginResourcesPath("projectPage.jsp"));
    myWebControllerManager = webControllerManager;
    myProjectSettingsManager = projectSettingsManager;
    setPosition(PositionConstraint.after("problems"));
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SProject project, @Nullable SUser user) {
    SharedResourcesTabProjectSettings settings = (SharedResourcesTabProjectSettings) myProjectSettingsManager.getSettings(project.getProjectId(), SERVICE_NAME);
    String str = request.getParameter("delete");
    if (str != null) {
      settings.remove(str);
    }
    str = request.getParameter("sample");
    if (str != null && "true".equals(str)) {
      settings.putSampleData();
    }
    project.persist();
    SharedResourcesBean bean = new SharedResourcesBean(project, settings.getSharedResourceNames());
    model.put("bean", bean);
  }
}
