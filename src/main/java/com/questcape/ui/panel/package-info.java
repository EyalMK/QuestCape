/**
 * Cooperating guide widgets and viewport/search state. Keep widget internals package-private;
 * extracting another subpackage requires a real contract, not public mutable fields or forwarding layers.
 * Application actions use ui.GuideActions; lifecycle/client attachment belongs to ui.sidebar.
 */
package com.questcape.ui.panel;
