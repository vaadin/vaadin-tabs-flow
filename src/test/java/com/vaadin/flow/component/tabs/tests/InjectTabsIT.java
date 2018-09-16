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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("inject-tabs")
public class InjectTabsIT extends AbstractComponentIT {

    @Test
    public void updateSelectionForInjectedTabs() {
        open();

        TestBenchElement template = $("inject-tabs").first();
        TestBenchElement tabs = template.$("vaadin-tabs").first();

        TestBenchElement javaTab = tabs.$("vaadin-tab").id("apiTab");

        javaTab.click();

        TestBenchElement msg = template.$(TestBenchElement.class).id("msg");
        Assert.assertEquals("apiTab, index=2", msg.getText());

        tabs.$("vaadin-tab").all().get(1).click();

        Assert.assertEquals(", index=1", msg.getText());

        tabs.$("vaadin-tab").first().click();
        Assert.assertEquals(", index=0", msg.getText());
    }
}
