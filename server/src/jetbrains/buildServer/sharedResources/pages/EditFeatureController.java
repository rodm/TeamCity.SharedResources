/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.sharedResources.pages;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.admin.projects.BuildFeatureBean;
import jetbrains.buildServer.controllers.admin.projects.BuildFeaturesBean;
import jetbrains.buildServer.controllers.admin.projects.EditBuildTypeFormFactory;
import jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.sharedResources.model.Lock;
import jetbrains.buildServer.sharedResources.server.feature.Locks;
import jetbrains.buildServer.sharedResources.server.feature.Resources;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static jetbrains.buildServer.sharedResources.SharedResourcesPluginConstants.EDIT_FEATURE_PATH_HTML;
import static jetbrains.buildServer.sharedResources.SharedResourcesPluginConstants.EDIT_FEATURE_PATH_JSP;

/**
 * @author Oleg Rybak
 */
public class EditFeatureController extends BaseController {

  @NotNull
  private final PluginDescriptor myDescriptor;

  @NotNull
  private final EditBuildTypeFormFactory myFormFactory;

  @NotNull
  private final Resources myResources;

  @NotNull
  private final Locks myLocks;


  public EditFeatureController(@NotNull final PluginDescriptor descriptor,
                               @NotNull final WebControllerManager web,
                               @NotNull final EditBuildTypeFormFactory formFactory,
                               @NotNull final Resources resources,
                               @NotNull final Locks locks) {
    myDescriptor = descriptor;
    myFormFactory = formFactory;
    myResources = resources;
    myLocks = locks;
    web.registerController(myDescriptor.getPluginResourcesPath(EDIT_FEATURE_PATH_HTML), this);

  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    final ModelAndView result = new ModelAndView(myDescriptor.getPluginResourcesPath(EDIT_FEATURE_PATH_JSP));
    final EditableBuildTypeSettingsForm form = myFormFactory.getOrCreateForm(request);
    final BuildFeaturesBean buildFeaturesBean = form.getBuildFeaturesBean();
    final String buildFeatureId = request.getParameter("featureId");
    final Map<String, Lock> locks = new HashMap<String, Lock>();
    final Map<String, Object> model = result.getModel();
    model.put("inherited", false);
    for (BuildFeatureBean bfb: buildFeaturesBean.getBuildFeatureDescriptors()) {
      if (buildFeatureId.equals(bfb.getDescriptor().getId())) {
        locks.putAll(myLocks.fromFeatureParameters(bfb.getDescriptor().getParameters()));
        model.put("inherited", bfb.isInherited());
        break;
      }
    }
    final SProject project = form.getProject();
    final SharedResourcesBean bean = new SharedResourcesBean(myResources.asCollection(project.getProjectId()));
    model.put("locks", locks);
    model.put("bean", bean);
    model.put("project", project);
    return result;
  }


}