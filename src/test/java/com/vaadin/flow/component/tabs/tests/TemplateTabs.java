/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@HtmlImport("ClientTabs.html")
@Route("template")
@Tag("client-tabs")
public class TemplateTabs extends PolymerTemplate<TemplateModel> {

    @Id("tabs")
    private Tabs tabs;

    private final Label message;

    public TemplateTabs() {
        message = new Label();
        message.setId("messsage-label");

        tabs.add(new Tab("ServerSide"));
        tabs.addSelectedChangeListener(selectedChangeEvent -> {
            String messageText;
            if (tabs.getSelectedTab() == null) {
                messageText = String
                        .format("Received selection for client-side tab with index '%s'",
                                tabs.getSelectedIndex());
            } else {
                messageText = String
                        .format("Received selection for server-side tab %s with index '%s'",
                                tabs.getSelectedTab(), tabs.getSelectedIndex());
            }
            message.setText(messageText);
        });

        getElement().appendChild(message.getElement());
    }
}
