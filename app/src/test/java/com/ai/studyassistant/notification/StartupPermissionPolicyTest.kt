package com.ai.studyassistant.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupPermissionPolicyTest {
    @Test fun firstOpeningRequestsPermission() {
        assertEquals(StartupPermissionAction.REQUEST, startupPermissionAction(false, true, false, false))
    }
    @Test fun denialCanBeRetriedOnNextOpeningWhenAndroidAllowsIt() {
        assertEquals(StartupPermissionAction.REQUEST, startupPermissionAction(false, true, true, true))
    }
    @Test fun blockedDialogUsesDismissibleSettingsExplanation() {
        assertEquals(StartupPermissionAction.SETTINGS, startupPermissionAction(false, true, true, false))
    }
    @Test fun grantedPermissionNeverPrompts() {
        assertEquals(StartupPermissionAction.NONE, startupPermissionAction(true, false, true, false))
    }
    @Test fun olderAndroidUsesSettingsWhenNotificationsAreDisabled() {
        assertEquals(StartupPermissionAction.SETTINGS, startupPermissionAction(false, false, false, false))
    }
}
