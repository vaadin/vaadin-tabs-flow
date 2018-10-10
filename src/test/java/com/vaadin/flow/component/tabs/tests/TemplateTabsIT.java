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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("template")
public class TemplateTabsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void verifyNoTabSelectedByDefault() {
        Assert.assertTrue("No message should be visible by default",
                findElement(By.id("message-label")).getText().isEmpty());
    }

    @Test
    public void selectingTab_showsAsClientOrServerAsExpected() {
        getInShadowRoot(findElement(By.id("template")), By.id("server-side")).click();

        Assert.assertEquals(
                "Received selection for server-side tab Tab{ServerSide} with index '3'",
                findElement(By.id("message-label")).getText());

        getInShadowRoot(findElement(By.id("template")), By.id("one")).click();

        Assert.assertEquals(
                "Received selection for client-side tab with index '0'",
                findElement(By.id("message-label")).getText());

        getInShadowRoot(findElement(By.id("template")), By.id("three")).click();

        Assert.assertEquals(
                "Received selection for client-side tab with index '2'",
                findElement(By.id("message-label")).getText());
    }

}
