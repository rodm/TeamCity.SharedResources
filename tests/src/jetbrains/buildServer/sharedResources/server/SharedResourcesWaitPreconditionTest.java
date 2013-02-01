/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.sharedResources.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.BuildPromotionEx;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.buildDistribution.BuildDistributorInput;
import jetbrains.buildServer.serverSide.buildDistribution.QueuedBuildInfo;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;
import jetbrains.buildServer.sharedResources.model.Lock;
import jetbrains.buildServer.sharedResources.server.feature.Locks;
import jetbrains.buildServer.sharedResources.server.feature.SharedResourcesFeatures;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Class {@code SharedResourcesWaitPreconditionTest}
 *
 * Contains tests for {@code SharedResourcesWaitPrecondition}
 *
 * @see SharedResourcesWaitPrecondition
 * *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
@SuppressWarnings("UnusedShould")
@TestFor(testForClass = SharedResourcesWaitPrecondition.class)
public class SharedResourcesWaitPreconditionTest extends BaseTestCase {

  private Mockery m;

  private Locks myLocks;

  private Resources myResources;

  private SharedResourcesFeatures myFeatures;

  private QueuedBuildInfo myQueuedBuild;

  private BuildPromotionEx myBuildPromotion;

  private BuildDistributorInput myBuildDistributorInput;

  private BuildTypeEx myBuildType;

  private final String myProjectId = "PROJECT_ID";

  private ParametersProvider myParametersProvider;



  /** Class under test*/
  private SharedResourcesWaitPrecondition myWaitPrecondition;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myLocks = m.mock(Locks.class);
    myResources = m.mock(Resources.class);
    myFeatures = m.mock(SharedResourcesFeatures.class);
    myBuildType = m.mock(BuildTypeEx.class);
    myQueuedBuild = m.mock(QueuedBuildInfo.class);
    myBuildPromotion = m.mock(BuildPromotionEx.class);
    myParametersProvider = m.mock(ParametersProvider.class);




    myBuildDistributorInput = m.mock(BuildDistributorInput.class);


    myWaitPrecondition = new SharedResourcesWaitPrecondition(myFeatures, myLocks, myResources);
  }

  @Test
  public void testNullBuildType() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myQueuedBuild).getBuildPromotionInfo();
      will(returnValue(myBuildPromotion));

      oneOf(myBuildPromotion).getBuildType();
      will(returnValue(null));

      oneOf(myBuildPromotion).getProjectId();
      will(returnValue(myProjectId));
    }});
    WaitReason result = myWaitPrecondition.canStart(myQueuedBuild, Collections.<QueuedBuildInfo, BuildAgent>emptyMap(), myBuildDistributorInput, false);
    assertNull(result);
  }

  @Test
  public void testNullProjectId() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myQueuedBuild).getBuildPromotionInfo();
      will(returnValue(myBuildPromotion));

      oneOf(myBuildPromotion).getBuildType();
      will(returnValue(myBuildType));

      oneOf(myBuildPromotion).getProjectId();
      will(returnValue(null));

    }});
    WaitReason result = myWaitPrecondition.canStart(myQueuedBuild, Collections.<QueuedBuildInfo, BuildAgent>emptyMap(), myBuildDistributorInput, false);
    assertNull(result);
  }

  @Test
  public void testNoFeaturesPresent() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myQueuedBuild).getBuildPromotionInfo();
      will(returnValue(myBuildPromotion));

      oneOf(myBuildPromotion).getBuildType();
      will(returnValue(myBuildType));

      oneOf(myBuildPromotion).getProjectId();
      will(returnValue(myProjectId));

      oneOf(myFeatures).featuresPresent(myBuildType);
      will(returnValue(false));

    }});
    WaitReason result = myWaitPrecondition.canStart(myQueuedBuild, Collections.<QueuedBuildInfo, BuildAgent>emptyMap(), myBuildDistributorInput, false);
    assertNull(result);
  }

  @Test
  public void testNoLocksInFeatures() throws Exception {
    final Collection<Lock> emptyLocks = Collections.emptyList();
    final Map<String, String> emptyParams = Collections.emptyMap();

    m.checking(new Expectations() {{
      oneOf(myQueuedBuild).getBuildPromotionInfo();
      will(returnValue(myBuildPromotion));

      oneOf(myBuildPromotion).getBuildType();
      will(returnValue(myBuildType));

      oneOf(myBuildPromotion).getProjectId();
      will(returnValue(myProjectId));

      oneOf(myFeatures).featuresPresent(myBuildType);
      will(returnValue(true));

      oneOf(myBuildPromotion).getParametersProvider();
      will(returnValue(myParametersProvider));

      oneOf(myParametersProvider).getAll();
      will(returnValue(emptyParams));

      oneOf(myLocks).fromBuildParameters(emptyParams);
      will(returnValue(emptyLocks));
    }});
    WaitReason result = myWaitPrecondition.canStart(myQueuedBuild, Collections.<QueuedBuildInfo, BuildAgent>emptyMap(), myBuildDistributorInput, false);
    assertNull(result);
  }

  @Test
  public void testLocksPresentSingleBuild() throws Exception {

  }

  @Test
  public void testMultipleBuildsLocksCrossing() throws Exception {

  }

  @Test
  public void testMultipleBuildsLocksNotCrossing() throws Exception {

  }

  @Test
  public void testBuildsFromOtherProjects() throws Exception {

  }
}
