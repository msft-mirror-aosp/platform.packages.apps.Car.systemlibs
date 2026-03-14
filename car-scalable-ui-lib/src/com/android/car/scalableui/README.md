# Car Scalable UI Library - Core Components

This directory: `src/com/android/car/scalableui`

Contains the model and event handling logic for the Car Scalable UI library, enabling event-driven UI state management and rendering for automotive displays.

## Core Concepts

*   **PanelState:** Defines the possible states (`Variant`s) and `Transition`s for a logical UI panel.
*   **Panel:** The actual UI element instance that renders the state on screen.
*   **Event:** Represents an occurrence that can trigger a state change in one or more panels.
*   **StateManager:** Orchestrates state transitions based on `Event`s and updates `Panel` properties.
*   **PanelPool:** Manages the instantiation and storage of `Panel` instances.
*   **PanelTransaction:** Bundles a set of panel updates (transitions and animations) to be applied together.

## Directory Structure

*   **`loader/`**: Handles loading of panel and state definitions from resources or configurations.
*   **`manager/`**: Contains the `StateManager`.
*   **`metrics/`**: Utilities for performance monitoring (e.g., jank).
*   **`model/`**: Data classes like `Event`, `PanelState`, `Variant`, `Transition`, `PanelTransaction`.
*   **`panel/`**: Contains the `Panel` interface and the `PanelPool`.

## Workflow Overview

1.  **Initialization:** Panel definitions (`PanelState` objects with `Variant`s and `Transition`s) are loaded, often by the `loader/` components.
2.  **Panel Creation:** `Panel` instances are created as needed via the `PanelPool`, using a `PanelCreatorDelegate` provided by the host environment (e.g., SystemUI).
3.  **Event Handling:** An `Event` occurs (e.g., user interaction, system status change).
4.  **State Transition:** The `StateManager.handleEvents()` method is called with the `Event`.
5.  **Transaction Building:** `StateManager` determines which `PanelState`s are affected, calculates the target `Variant`s, and builds a `PanelTransaction` containing necessary `Animator`s and callbacks.
6.  **Transaction Execution:** The caller executes the `PanelTransaction`, triggering animations and updating `Panel` properties.
7.  **UI Update:** The `Panel` implementations render the changes on the screen.

---

## Deep Dives

### 1. StateManager (`manager/StateManager.java`)

Singleton responsible for the core state transition logic.

**Responsibilities:**

*   Stores all known `PanelState` objects.
*   Processes incoming `Event`s to determine state changes.
*   Builds `PanelTransaction`s to encapsulate UI updates.
*   Interacts with `PanelPool` to get `Panel` instances to apply state to.

**`handleEvents(List<Event> events, boolean force)` Method:**

This method is the heart of the event-driven state changes.

1.  **Iterate Panels:** Loops through each registered `PanelState`.
2.  **Determine Final Transition:** Uses `getTransitionForEvents` to find the single resulting `Transition` after sequentially simulating the effects of each `Event` in the input list. The events are evaluated in order, and the state resulting from one event becomes the starting point for the next.
3.  **Skip Unchanged:** If the final target `Variant` is the same as the current one and `force` is not true, no action is taken for this panel (with an exception for `KeyFrameVariant`).
4.  **Build Transaction:**
    *   If the `Transition` has an `Animator`, it's added to the `PanelTransaction.Builder`. The `PanelState`'s current variant is updated, and animation listeners are set to call `applyState` on completion.
    *   If no `Animator`, the state is applied immediately via `applyState()` if the panel is not already animating.
5.  **Return Transaction:** Returns the aggregated `PanelTransaction`.

### 2. PanelTransaction (`model/PanelTransaction.java`)

Represents a set of operations to be applied to one or more panels, usually as a single logical unit.

**Key Components:**

*   `mTransactionMap`: `Map<String, Transition>` - Panel ID to the `Transition` to be applied.
*   `mAnimatorMap`: `Map<String, Animator>` - Panel ID to the `Animator` for the transition.
*   `mLockededPanelIdSet`: `Set<String>` - Panels to exclude from changes in this transaction.
*   `mHasWindowChanges`: `boolean` - Hints if Window Manager operations might be needed.
*   `mAnimationStartCallbackRunnable`: `Runnable` - Executed before animations start.
*   `mAnimationEndCallbackRunnable`: `Runnable` - Executed after animations end.

**Execution Flow:**

1.  Run Start Callback.
2.  Start all `Animator`s.
3.  Animations update `Panel` properties.
4.  Run End Callback.

### 3. PanelPool (`panel/PanelPool.java`)

Singleton managing the creation and retrieval of `Panel` instances.

**Key Features:**

*   Ensures unique `Panel` instance per ID.
*   Delegates actual `Panel` creation to a `PanelCreatorDelegate` (provided externally).
*   Caches instances for efficient retrieval via `getOrCreatePanel()`.

### 4. Panel vs PanelState Explained

*   **`PanelState` (`model/PanelState.java`): The Definition**
    *   **WHAT:** Describes the possible states (`Variant`s) a panel can be in, the transitions between them, and its current logical state (e.g., which `Variant` is active).
    *   It's a data model managed by `StateManager`.

*   **`Panel` (`panel/Panel.java`): The Instance**
    *   **HOW:** The actual object that renders the UI on the screen. It implements methods to change visual properties (bounds, alpha, etc.).
    *   Instances are managed by `PanelPool` and their concrete type depends on the rendering layer (e.g., View-based, SurfaceControl-based).

**Interaction:**
`StateManager` reads the `PanelState` to know what the UI *should* look like. Then, it gets the corresponding `Panel` instance from the `PanelPool` and calls methods on the `Panel` to make the on-screen representation match the desired state in `PanelState`.

---