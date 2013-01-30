package jetbrains.buildServer.sharedResources.server.feature;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.serverSide.ResolvedSettings;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.sharedResources.server.SharedResourcesBuildFeature;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class {@code SharedResourcesFeaturesImplTest}
 *
 * Contains tests for SharedResourcesFeaturesImpl
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
@TestFor (testForClass = SharedResourcesFeaturesImpl.class)
public class SharedResourcesFeaturesImplTest extends BaseTestCase {

  /** Number of test samples for collections */
  private final int NUM = 10;

  private Mockery m;

  /** Mocked build type */
  private SBuildType myBuildType;

  /** Descriptors, that represent {@code SharedResourcesBuildFeature} */
  private List<SBuildFeatureDescriptor> myValidDescriptors;

  /** Descriptors that represent other features*/
  private List<SBuildFeatureDescriptor> myInvalidDescriptors;

  /** Contains all feature descriptors for buildType */
  private List<SBuildFeatureDescriptor> myAllFeatureDescriptors;

  /** Class under test */
  private SharedResourcesFeatures mySharedResourcesFeatures;

  /** Resolved setting of */
  private ResolvedSettings myResolvedSettings;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();

    myBuildType = m.mock(SBuildType.class, "my-build-type");

    myInvalidDescriptors = new ArrayList<SBuildFeatureDescriptor>(NUM);
    myValidDescriptors = new ArrayList<SBuildFeatureDescriptor>(NUM);

    for (int i = 0; i < NUM; i++) {
      myInvalidDescriptors.add(m.mock(SBuildFeatureDescriptor.class, "invalid-descriptor-" + i));
      myValidDescriptors.add(m.mock(SBuildFeatureDescriptor.class, "valid-descriptor-" + i));
    }

    myAllFeatureDescriptors = new ArrayList<SBuildFeatureDescriptor>();
    myAllFeatureDescriptors.addAll(myInvalidDescriptors);
    myAllFeatureDescriptors.addAll(myValidDescriptors);

    myResolvedSettings = m.mock(ResolvedSettings.class);

    mySharedResourcesFeatures = new SharedResourcesFeaturesImpl();
  }


  @Test
  public void testSearchForFeatures() {
    m.checking(new Expectations() {{
      oneOf(myBuildType).getBuildFeatures();
      will(returnValue(myAllFeatureDescriptors));

      for (int i = 0; i < NUM; i++) {
        oneOf(myInvalidDescriptors.get(i)).getType();
        will(returnValue("$$some_other_type$$"));

        oneOf(myValidDescriptors.get(i)).getType();
        will(returnValue(SharedResourcesBuildFeature.FEATURE_TYPE));

        oneOf(myValidDescriptors.get(i)).getParameters();
        will(returnValue(Collections.emptyMap()));
      }
    }});

    final Collection<SharedResourcesFeature> myFeatures = mySharedResourcesFeatures.searchForFeatures(myBuildType);
    assertNotNull(myFeatures);
    assertNotEmpty(myFeatures);
    // todo: fix test
//    for (SBuildFeatureDescriptor d: myValidDescriptors) {
//      assertContains(myFeatures, d);
//    }
    m.assertIsSatisfied();
  }

  @Test
  public void testSearchForResolvedFeatures() {
    m.checking(new Expectations() {{

      oneOf(myBuildType).getResolvedSettings();
      will(returnValue(myResolvedSettings));

      oneOf(myResolvedSettings).getBuildFeatures();
      will(returnValue(myAllFeatureDescriptors));

      for (int i = 0; i < NUM; i++) {
        oneOf(myInvalidDescriptors.get(i)).getType();
        will(returnValue("$$some_other_type$$"));

        oneOf(myValidDescriptors.get(i)).getType();
        will(returnValue(SharedResourcesBuildFeature.FEATURE_TYPE));

        oneOf(myValidDescriptors.get(i)).getId();
        will(returnValue("some-id-" + i));

        oneOf(myBuildType).isEnabled("some-id-" + i);
        will(returnValue(i % 2 == 0));

        if (i % 2 == 0) {
          oneOf(myValidDescriptors.get(i)).getParameters();
          will(returnValue(Collections.emptyMap()));
        }
      }
    }});

    final Collection<SharedResourcesFeature> myFeatures = mySharedResourcesFeatures.searchForResolvedFeatures(myBuildType);
    assertNotNull(myFeatures);
    assertNotEmpty(myFeatures);
    assertEquals(NUM / 2, myFeatures.size());
    // todo: fix test
//    for (int i = 0; i < NUM; i+= 2) {
//      assertContains(myFeatures, myValidDescriptors.get(i));
//    }
    m.assertIsSatisfied();
  }


}