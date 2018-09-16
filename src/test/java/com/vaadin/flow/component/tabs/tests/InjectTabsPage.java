/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("inject-tabs")
@HtmlImport("frontend://inject-tabs.html")
@Route("inject-tabs")
public class InjectTabsPage extends PolymerTemplate<TemplateModel>
        implements HasComponents {

    @Id
    private Tabs tabs;

    public InjectTabsPage() {
        Tab apiTab = new Tab();
        apiTab.setLabel("API Tab");
        apiTab.setId("apiTab");
        tabs.add(apiTab);
        tabs.addSelectedChangeListener(event -> handleSelection());
    }

    private void handleSelection() {
        Tab selectedTab = tabs.getSelectedTab();
        String selectedTabId = selectedTab == null ? ""
                : selectedTab.getId().orElse("");
        getElement().setProperty("selectedTab",
                selectedTabId + ", index=" + tabs.getSelectedIndex());
    }
}
