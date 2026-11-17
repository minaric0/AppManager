// SPDX-License-Identifier: GPL-3.0-or-later
package io.github.muntashirakon.AppManager.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import io.github.muntashirakon.AppManager.actions.ActionKind;
import io.github.muntashirakon.AppManager.actions.ActionParameters;

public class SamsungA17TuningTest {
    @Test public void matchesExactTarget() {
        assertTrue(SamsungA17Tuning.isTargetDevice("samsung", "SM-A175N", 37));
        assertTrue(SamsungA17Tuning.isTargetDevice("SAMSUNG", "sm-a175n", 38));
        assertFalse(SamsungA17Tuning.isTargetDevice("samsung", "SM-A175N", 36));
        assertFalse(SamsungA17Tuning.isTargetDevice("samsung", "SM-A356N", 37));
        assertFalse(SamsungA17Tuning.isTargetDevice("other", "SM-A175N", 37));
    }
    @Test public void tuningRespectsManualOverride() {
        assertEquals(4, SamsungA17Tuning.recommendedThreadCount(0, 8, true));
        assertEquals(8, SamsungA17Tuning.recommendedThreadCount(0, 8, false));
        assertEquals(2, SamsungA17Tuning.recommendedThreadCount(0, 2, true));
        assertEquals(6, SamsungA17Tuning.recommendedThreadCount(6, 8, true));
        assertEquals(8, SamsungA17Tuning.recommendedThreadCount(100, 8, true));
    }
    @Test public void protectsCorePackagesAndPrivilegedSharedUids() {
        assertTrue(SamsungA17Tuning.isProtectedPackage("com.android.systemui", -1));
        assertTrue(SamsungA17Tuning.isProtectedPackage("com.example.service", 1000));
        assertTrue(SamsungA17Tuning.isProtectedPackage("com.example.service", 101000));
        assertFalse(SamsungA17Tuning.isProtectedPackage("com.example.app", 10100));
        assertFalse(SamsungA17Tuning.isProtectedPackage("com.example.app", -1));
    }
    @Test public void keepsRestorativeActionsAvailable() {
        assertTrue(SamsungA17Tuning.isGuardedMutation(ActionKind.UNINSTALL, new ActionParameters()));
        assertTrue(SamsungA17Tuning.isGuardedMutation(ActionKind.FREEZE,
                new ActionParameters().put("freeze", true)));
        assertFalse(SamsungA17Tuning.isGuardedMutation(ActionKind.FREEZE,
                new ActionParameters().put("freeze", false)));
        assertTrue(SamsungA17Tuning.isGuardedMutation(ActionKind.COMPONENT_STATE,
                new ActionParameters().put("blocked", true)));
        assertTrue(SamsungA17Tuning.isGuardedMutation(ActionKind.PERMISSION_STATE,
                new ActionParameters().put("granted", false)));
        assertFalse(SamsungA17Tuning.isGuardedMutation(ActionKind.PERMISSION_STATE,
                new ActionParameters().put("granted", true)));
    }
}
