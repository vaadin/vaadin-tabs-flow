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

package com.vaadin.flow.component.tabs;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code vaadin-tabs} element.
 * <p>
 * {@link Tab} components can be added to this component with the
 * {@link #add(Tab...)} method or the {@link #Tabs(Tab...)} constructor. The Tab
 * components added to it can be selected with the
 * {@link #setSelectedIndex(int)} or {@link #setSelectedTab(Tab)} methods.
 * Removing the selected tab from the component changes the selection to the
 * next available tab.
 * <p>
 * <strong>Note:</strong> Adding or removing Tab components via the Element API,
 * eg. {@code tabs.getElement().insertChild(0, tab.getElement()); }, doesn't
 * update the selected index, so it may cause the selected tab to change
 * unexpectedly.
 *
 * @author Vaadin Ltd.
 */
public class Tabs extends GeneratedVaadinTabs<Tabs>
        implements HasOrderedComponents<Tabs>, HasSize {

    private static final String SELECTED = "selected";

    private Tab selectedTab;

    private transient Tab previouslySelectedTab;
    private int previouslySelectedIndex;

    private int clientSideZeroIndex = -1;

    /**
     * The valid orientations of {@link Tabs} instances.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    /**
     * Constructs an empty new object with {@link Orientation#HORIZONTAL
     * HORIZONTAL} orientation.
     */
    public Tabs() {
        setSelectedIndex(-1);
        previouslySelectedIndex = -1;
        getElement().addPropertyChangeListener(SELECTED,
                event -> updateSelectedTab(event.isUserOriginated()));
    }

    /**
     * Constructs a new object enclosing the given tabs, with
     * {@link Orientation#HORIZONTAL HORIZONTAL} orientation.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public Tabs(Tab... tabs) {
        this();
        add(tabs);
    }

    /**
     * Adds the given tabs to the component.
     *
     * @param tabs
     *            the tabs to enclose
     */
    public void add(Tab... tabs) {
        add((Component[]) tabs);
    }

    @Override
    public void add(Component... components) {
        boolean wasEmpty = getComponentCount() == 0;
        HasOrderedComponents.super.add(components);
        if (wasEmpty) {
            assert getSelectedIndex() == -1;
            setSelectedIndex(0);
        } else {
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removing components before the selected tab will decrease the
     * {@link #getSelectedIndex() selected index} to avoid changing the selected
     * tab. Removing the selected tab will select the next available tab.
     */
    @Override
    public void remove(Component... components) {
        int lowerIndices = (int) Stream.of(components).map(this::indexOf)
                .filter(index -> index >= 0 && index < getSelectedIndex())
                .count();

        HasOrderedComponents.super.remove(components);

        // Prevents changing the selected tab
        int newSelectedIndex = getSelectedIndex() - lowerIndices;

        // In case the last tab was removed
        if (newSelectedIndex > 0 && newSelectedIndex >= getComponentCount()) {
            newSelectedIndex = getComponentCount() - 1;
        }

        if (getComponentCount() == 0) {
            newSelectedIndex = -1;
        }

        if (newSelectedIndex != getSelectedIndex()) {
            setSelectedIndex(newSelectedIndex);
        } else {
            selectedTab = (Tab) getChildren().skip(newSelectedIndex).findFirst()
                    .get();
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will reset the {@link #getSelectedIndex() selected index} to zero.
     */
    @Override
    public void removeAll() {
        HasOrderedComponents.super.removeAll();
        if (getSelectedIndex() > -1) {
            setSelectedIndex(-1);
        } else {
            selectedTab = null;
            updateSelectedTab(false);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adding a component before the currently selected tab will increment the
     * {@link #getSelectedIndex() selected index} to avoid changing the selected
     * tab.
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasOrderedComponents.super.addComponentAtIndex(index, component);

        if (index <= getSelectedIndex()) {
            // Prevents changing the selected tab
            setSelectedIndex(getSelectedIndex() + 1);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Replacing the currently selected tab will make the new tab selected.
     */
    @Override
    public void replace(Component oldComponent, Component newComponent) {
        HasOrderedComponents.super.replace(oldComponent, newComponent);
        updateSelectedTab(false);
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    public static class SelectedChangeEvent extends ComponentEvent<Tabs> {
        public SelectedChangeEvent(Tabs source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                this,
                context -> ui.getPage().executeJavaScript(
                        "$0.addEventListener('items-changed', "
                                + "function(){ this.$server.itemsChanged(); });",
                        getElement())));
    }

    /**
     * Adds a listener for {@link SelectedChangeEvent}.
     *
     * @param listener
     *            the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addSelectedChangeListener(
            ComponentEventListener<SelectedChangeEvent> listener) {
        return addListener(SelectedChangeEvent.class, listener);
    }

    /**
     * Gets the zero-based index of the currently selected tab.
     *
     * @return the zero-based index of the selected tab, or -1 if none of the
     *         tabs is selected
     */
    @Synchronize(property = SELECTED, value = "selected-changed")
    public int getSelectedIndex() {
        return getElement().getProperty(SELECTED, -1);
    }

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex
     *            the zero-based index of the selected tab, -1 to unselect all
     */
    public void setSelectedIndex(int selectedIndex) {
        previouslySelectedIndex = selectedIndex;
        getElement().setProperty(SELECTED, selectedIndex);
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    public Tab getSelectedTab() {
        return selectedTab;
    }

    /**
     * Selects the given tab.
     *
     * @param selectedTab
     *            the tab to select, {@code null} to unselect all
     * @throws IllegalArgumentException
     *             if {@code selectedTab} is not a child of this component
     */
    public void setSelectedTab(Tab selectedTab) {
        if (selectedTab == null) {
            setSelectedIndex(-1);
            return;
        }

        if (selectedTab.getParent().orElse(null) != this) {
            throw new IllegalArgumentException(
                    "Tab to select must be a child: " + selectedTab);
        }
        this.selectedTab = selectedTab;
        getElement().executeJavaScript("var i = 0; var child = $0;\n "
                + "while( (child = child.previousSibling) != null && child.tagName !=null &&  child.tagName.toLowerCase() =='vaadin-tab') "
                + "{ i++; }\n this.selected = i;", selectedTab.getElement());
    }

    /**
     * Gets the orientation of this tab sheet.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        String orientation = getElement().getProperty("orientation");
        if (orientation != null) {
            return Orientation.valueOf(orientation.toUpperCase(Locale.ROOT));
        }
        return Orientation.HORIZONTAL;
    }

    /**
     * Sets the orientation of this tab sheet.
     *
     * @param orientation
     *            the orientation
     */
    public void setOrientation(Orientation orientation) {
        getElement().setProperty("orientation",
                orientation.name().toLowerCase());
    }

    /**
     * Sets the flex grow property of all enclosed tabs. The flex grow property
     * specifies what amount of the available space inside the layout the
     * component should take up, proportionally to the other components.
     * <p>
     * For example, if all components have a flex grow property value set to 1,
     * the remaining space in the layout will be distributed equally to all
     * components inside the layout. If you set a flex grow property of one
     * component to 2, that component will take twice the available space as the
     * other components, and so on.
     * <p>
     * Setting to flex grow property value 0 disables the expansion of the
     * component. Negative values are not allowed.
     *
     * @param flexGrow
     *            the proportion of the available space the enclosed tabs should
     *            take up
     * @throws IllegalArgumentException
     *             if {@code flexGrow} is negative
     */
    public void setFlexGrowForEnclosedTabs(double flexGrow) {
        if (flexGrow < 0) {
            throw new IllegalArgumentException(
                    "Flex grow property must not be negative");
        }
        getChildren().forEach(tab -> ((Tab) tab).setFlexGrow(flexGrow));
    }

    @Override
    protected void fireEvent(ComponentEvent<?> componentEvent) {
        if (componentEvent instanceof SelectedChangeEvent
                && clientSideZeroIndex == -1) {
            return;
        }
        super.fireEvent(componentEvent);
    }

    @ClientCallable
    private void itemsChanged() {
        if (clientSideZeroIndex == 0) {
            // Initially the clientSideZeroIndex's value is -1 which means we
            // don't know the index. Then it's set via this call. If at some
            // point the index becomes zero then it means that all client side
            // tabs has been removed at some point and they can't be restored
            // anymore. So no need to update this index anymore.
            return;
        }
        getElement().executeJavaScript(
                "this.$server.setZeroIndex(this.items.length -$0);",
                (int) getChildren().count());
    }

    @ClientCallable
    private void setZeroIndex(int index) {
        clientSideZeroIndex = index;
        updateSelectedTab(true);
    }

    private void updateSelectedTab(boolean changedFromClient) {
        if (changedFromClient && getSelectedIndex() < -1) {
            setSelectedIndex(-1);
            return;
        }

        Tab currentlySelected = null;

        if (changedFromClient) {
            int selectedIndex = getSelectedIndex();
            if (clientSideZeroIndex == -1) {
                LoggerFactory.getLogger(Tabs.class).debug(
                        "The selection event is ignored at this point since the number of tabs is not known yet");
                return;
            } else if (selectedIndex >= clientSideZeroIndex) {
                Component selectedComponent = getComponentAt(
                        selectedIndex - clientSideZeroIndex);
                if (!(selectedComponent instanceof Tab)) {
                    throw new IllegalStateException(
                            "Illegal component inside Tabs: "
                                    + selectedComponent);
                }
                currentlySelected = (Tab) selectedComponent;
            }
        } else {
            currentlySelected = getSelectedTab();
        }

        doUpdateSelectedTab(changedFromClient, currentlySelected);
    }

    private void doUpdateSelectedTab(boolean changedFromClient,
            Tab currentlySelected) {
        int selectedIndex = getSelectedIndex();
        /*
         * We should also track selectedIndex changes to be able to send
         * SelectedChangeEvents for the tabs which are not available on the
         * server side (pure client-side in case of @Id injection). The Tab
         * instance on the server side is null but the index still makes sense
         * and its value is updated.
         */
        if (Objects.equals(currentlySelected, previouslySelectedTab)
                && previouslySelectedIndex == selectedIndex) {
            return;
        }

        if (currentlySelected == null || currentlySelected.isEnabled()) {
            previouslySelectedTab = currentlySelected;
            previouslySelectedIndex = selectedIndex;
            getChildren().filter(Tab.class::isInstance).map(Tab.class::cast)
                    .forEach(tab -> tab.setSelected(false));

            if (previouslySelectedTab != null) {
                previouslySelectedTab.setSelected(true);
            }
            selectedTab = currentlySelected;
            fireEvent(new SelectedChangeEvent(this, changedFromClient));
        } else {
            updateEnabled(currentlySelected);
            setSelectedTab(previouslySelectedTab);
        }
    }

    private void updateEnabled(Tab tab) {
        boolean enabled = tab.isEnabled();
        Serializable rawValue = tab.getElement().getPropertyRaw("disabled");
        if (rawValue instanceof Boolean) {
            // convert the boolean value to a String to force update the
            // property value. Otherwise since the provided value is the same as
            // the current one the update don't do anything.
            tab.getElement().setProperty("disabled",
                    enabled ? null : Boolean.TRUE.toString());
        } else {
            tab.setEnabled(enabled);
        }
    }

}
