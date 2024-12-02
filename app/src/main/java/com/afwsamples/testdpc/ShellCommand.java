/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afwsamples.testdpc;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afwsamples.testdpc.common.Util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a CLI (command-line interface) to TestDPC through {@code dumpsys}.
 *
 * <p>Usage: {@code adb shell dumpsys activity service --user USER_ID com.afwsamples.testdpc CMD}.
 *
 */
final class ShellCommand {
    private static final String TAG = "TestDPCShellCommand";

    private static final String CMD_DUMP = "dump";
    private static final String CMD_CREATE_USER = "create-user";
    private static final String CMD_SET_USER_ICON = "set-user-icon";
    private static final String CMD_REMOVE_USER = "remove-user";
    private static final String CMD_SWITCH_USER = "switch-user";
    private static final String CMD_START_USER_BG = "start-user-in-background";
    private static final String CMD_STOP_USER = "stop-user";
    private static final String CMD_LIST_USER_RESTRICTIONS = "list-user-restrictions";
    private static final String CMD_SET_USER_RESTRICTION = "set-user-restriction";
    private static final String CMD_IS_USER_AFFILIATED = "is-user-affiliated";
    private static final String CMD_SET_AFFILIATION_IDS = "set-affiliation-ids";
    private static final String CMD_GET_AFFILIATION_IDS = "get-affiliation-ids";
    private static final String CMD_HELP = "help";
    private static final String CMD_LOCK_NOW = "lock-now";
    private static final String CMD_REBOOT = "reboot";
    private static final String CMD_WIPE_DATA = "wipe-data";
    private static final String CMD_REQUEST_BUGREPORT = "request-bugreport";
    private static final String CMD_SET_NETWORK_LOGGING = "set-network-logging";
    private static final String CMD_SET_ORGANIZATION_NAME = "set-organization-name";
    private static final String CMD_GET_ORGANIZATION_NAME = "get-organization-name";
    private static final String CMD_SET_USER_CONTROL_DISABLED_PACKAGES =
            "set-user-control-disabled-packages";
    private static final String CMD_GET_USER_CONTROL_DISABLED_PACKAGES =
            "get-user-control-disabled-packages";
    private static final String CMD_REMOVE_ACTIVE_ADMIN = "remove-active-admin";
    private static final String CMD_CLEAR_DEVICE_OWNER = "clear-device-owner";
    private static final String CMD_CLEAR_PROFILE_OWNER = "clear-profile-owner";
    private static final String CMD_SET_PASSWORD_QUALITY = "set-password-quality";
    private static final String CMD_GET_PASSWORD_QUALITY = "get-password-quality";
    private static final String CMD_TRANSFER_OWNERSHIP = "transfer-ownership";
    private static final String CMD_SET_SUSPENDED_PACKAGES = "set-suspended-packages";
    private static final String CMD_IS_SUSPENDED_PACKAGE = "is-suspended-packages";
    private static final String CMD_SET_PERSONAL_APPS_SUSPENDED = "set-personal-apps-suspended";
    private static final String CMD_GET_PERSONAL_APPS_SUSPENDED_REASONS
            = "get-personal-apps-suspended-reasons";
    private static final String CMD_ENABLE_SYSTEM_APP = "enable-system-app";
    private static final String CMD_LIST_DISABLED_SYSTEM_APPS = "list-disabled-system-apps";
    private static final String CMD_SET_HIDDEN_PACKAGE = "set-hidden-package";
    private static final String CMD_IS_HIDDEN_PACKAGE = "is-hidden-package";
    private static final String CMD_SET_LOCK_TASK_PACKAGES = "set-lock-task-packages";
    private static final String CMD_GET_LOCK_TASK_PACKAGES = "get-lock-task-packages";
    private static final String CMD_IS_LOCK_TASK_PERMITTED = "is-lock-task-permitted";
    private static final String CMD_SET_LOCK_TASK_FEATURES = "set-lock-task-features";
    private static final String CMD_GET_LOCK_TASK_FEATURES = "get-lock-task-features";
    private static final String CMD_SET_APP_RESTRICTIONS = "set-app-restrictions";
    private static final String CMD_GET_APP_RESTRICTIONS = "get-app-restrictions";
    private static final String CMD_SET_PERMISSION_GRANT_STATE = "set-permission-grant-state";
    private static final String CMD_GET_PERMISSION_GRANT_STATE = "get-permission-grant-state";
    private static final String CMD_SET_LOCATION_ENABLED = "set-location-enabled";
    private static final String CMD_IS_LOCATION_ENABLED = "is-location-enabled";
    private static final String CMD_SET_DEVICE_OWNER_LOCKSCREEN_INFO
            = "set-device-owner-lockscreen-info";
    private static final String CMD_GET_DEVICE_OWNER_LOCKSCREEN_INFO
            = "get-device-owner-lockscreen-info";
    private static final String CMD_SET_KEYGUARD_DISABLED = "set-keyguard-disabled";
    private static final String CMD_SET_KEYGUARD_DISABLED_FEATURES =
            "set-keyguard-disabled-features";
    private static final String CMD_GET_KEYGUARD_DISABLED_FEATURES =
            "get-keyguard-disabled-features";

    // Commands for APIs added on Android S
    private static final String CMD_SET_PERMITTED_INPUT_METHODS_PARENT =
        "set-permitted-input-methods-parent";
    private static final String CMD_SET_USB_DATA_SIGNALING_ENABLED =
        "set-usb-data-signaling-enabled";
    private static final String CMD_LIST_FOREGROUND_USERS = "list-foreground-users";
    private static final String CMD_IS_FOREGROUND_USER = "is-foreground-user";

    private static final String ARG_FLAGS = "--flags";

    private final Context mContext;
    private final PrintWriter mWriter;
    private final String[] mArgs;
    private final DevicePolicyManagerGateway mDevicePolicyManagerGateway;

    public ShellCommand(@NonNull Context context, @NonNull PrintWriter writer,
            @Nullable String[] args) {
        mContext = context;
        mWriter = writer;
        mArgs = args;
        mDevicePolicyManagerGateway = new DevicePolicyManagerGatewayImpl(context);

        Log.d(TAG, "args=" + Arrays.toString(args));
    }

    public void run() {
        if (mArgs == null || mArgs.length == 0) {
            showUsage();
            return;
        }
        String cmd = mArgs[0];
        switch (cmd) {
            case CMD_DUMP:
                dumpState();
                break;
            case CMD_HELP:
                showUsage();
                break;
            case CMD_CREATE_USER:
                execute(() -> createUser());
                break;
            case CMD_SET_USER_ICON:
                execute(() -> setUserIcon());
                break;
            case CMD_REMOVE_USER:
                execute(() -> removeUser());
                break;
            case CMD_SWITCH_USER:
                execute(() -> switchUser());
                break;
            case CMD_START_USER_BG:
                execute(() -> startUserInBackground());
                break;
            case CMD_STOP_USER:
                execute(() -> stopUser());
                break;
            case CMD_IS_USER_AFFILIATED:
                execute(() -> isUserAffiliated());
                break;
            case CMD_SET_AFFILIATION_IDS:
                execute(() -> setAffiliationIds());
                break;
            case CMD_GET_AFFILIATION_IDS:
                execute(() -> getAffiliationIds());
                break;
            case CMD_LIST_USER_RESTRICTIONS:
                execute(() -> listUserRestrictions());
                break;
            case CMD_SET_USER_RESTRICTION:
                execute(() -> setUserRestriction());
                break;
            case CMD_LOCK_NOW:
                execute(() -> lockNow());
                break;
            case CMD_REBOOT:
                execute(() -> reboot());
                break;
            case CMD_WIPE_DATA:
                execute(() -> wipeData());
                break;
            case CMD_REQUEST_BUGREPORT:
                execute(() -> requestBugreport());
                break;
            case CMD_SET_NETWORK_LOGGING:
                execute(() -> setNetworkLogging());
                break;
            case CMD_SET_ORGANIZATION_NAME:
                execute(() -> setOrganizationName());
                break;
            case CMD_GET_ORGANIZATION_NAME:
                execute(() -> getOrganizationName());
                break;
            case CMD_SET_USER_CONTROL_DISABLED_PACKAGES:
                execute(() -> setUserControlDisabledPackages());
                break;
            case CMD_GET_USER_CONTROL_DISABLED_PACKAGES:
                execute(() -> getUserControlDisabledPackages());
                break;
            case CMD_REMOVE_ACTIVE_ADMIN:
                execute(() -> removeActiveAdmin());
                break;
            case CMD_CLEAR_DEVICE_OWNER:
                execute(() -> clearDeviceOwner());
                break;
            case CMD_CLEAR_PROFILE_OWNER:
                execute(() -> clearProfileOwner());
                break;
            case CMD_SET_PERMITTED_INPUT_METHODS_PARENT:
                execute(() -> setPermittedInputMethodsOnParent());
                break;
            case CMD_LIST_FOREGROUND_USERS:
                execute(() -> listForegroundUsers());
                break;
            case CMD_IS_FOREGROUND_USER:
                execute(() -> isForegroundUser());
                break;
            case CMD_SET_PASSWORD_QUALITY:
                execute(() -> setPasswordQuality());
                break;
            case CMD_GET_PASSWORD_QUALITY:
                execute(() -> getPasswordQuality());
                break;
            case CMD_SET_USB_DATA_SIGNALING_ENABLED:
                execute(() -> setUsbDataSignalingEnabled());
                break;
            case CMD_TRANSFER_OWNERSHIP:
                execute(() -> transferOwnership());
                break;
            case CMD_SET_SUSPENDED_PACKAGES:
                execute(() -> setSuspendedPackages());
                break;
            case CMD_IS_SUSPENDED_PACKAGE:
                execute(() -> isSuspendedPackage());
                break;
            case CMD_SET_PERSONAL_APPS_SUSPENDED:
                execute(() -> setPersonalAppsSuspended());
                break;
            case CMD_GET_PERSONAL_APPS_SUSPENDED_REASONS:
                execute(() -> getPersonalAppsSuspendedReasons());
                break;
            case CMD_SET_HIDDEN_PACKAGE:
                execute(() -> setHiddenPackage());
                break;
            case CMD_IS_HIDDEN_PACKAGE:
                execute(() -> isHiddenPackage());
                break;
            case CMD_ENABLE_SYSTEM_APP:
                execute(() -> enableSystemApp());
                break;
            case CMD_LIST_DISABLED_SYSTEM_APPS:
                execute(() -> listDisabledSystemApps());
                break;
            case CMD_SET_LOCK_TASK_PACKAGES:
                execute(() -> setLockTaskPackages());
                break;
            case CMD_GET_LOCK_TASK_PACKAGES:
                execute(() -> getLockTaskPackages());
                break;
            case CMD_IS_LOCK_TASK_PERMITTED:
                execute(() -> isLockTaskPermitted());
                break;
            case CMD_SET_LOCK_TASK_FEATURES:
                execute(() -> setLockTaskFeatures());
                break;
            case CMD_GET_LOCK_TASK_FEATURES:
                execute(() -> getLockTaskFeatures());
                break;
            case CMD_SET_APP_RESTRICTIONS:
                execute(() -> setAppRestrictions());
                break;
            case CMD_GET_APP_RESTRICTIONS:
                execute(() -> getAppRestrictions());
                break;
            case CMD_SET_PERMISSION_GRANT_STATE:
                execute(() -> setPermissionGrantState());
                break;
            case CMD_GET_PERMISSION_GRANT_STATE:
                execute(() -> getPermissionGrantState());
                break;
            case CMD_SET_LOCATION_ENABLED:
                execute(() -> setLocationEnabled());
                break;
            case CMD_IS_LOCATION_ENABLED:
                execute(() -> isLocationEnabled());
                break;
            case CMD_SET_DEVICE_OWNER_LOCKSCREEN_INFO:
                execute(() -> setDeviceOwnerLockScreenInfo());
                break;
            case CMD_GET_DEVICE_OWNER_LOCKSCREEN_INFO:
                execute(() -> getDeviceOwnerLockScreenInfo());
                break;
            case CMD_SET_KEYGUARD_DISABLED:
                execute(() -> setKeyguardDisabled());
                break;
            case CMD_SET_KEYGUARD_DISABLED_FEATURES:
                execute(() -> setKeyguardDisabledFeatures());
                break;
            case CMD_GET_KEYGUARD_DISABLED_FEATURES:
                execute(() -> getKeyguardDisabledFeatures());
                break;
            default:
                mWriter.printf("Invalid command: %s\n\n", cmd);
                showUsage();
        }
    }

    private void dumpState() {
        mWriter.printf("isDeviceOwner: %b\n", mDevicePolicyManagerGateway.isDeviceOwnerApp());
        mWriter.printf("isProfileOwner: %b\n", mDevicePolicyManagerGateway.isProfileOwnerApp());
        mWriter.printf("isOrganizationOwnedDeviceWithManagedProfile: %b\n",
                mDevicePolicyManagerGateway.isOrganizationOwnedDeviceWithManagedProfile());
        if (Util.isAtLeastS()) {
            mWriter.printf("isHeadlessSystemUserMode: %b\n",
                    mDevicePolicyManagerGateway.isHeadlessSystemUserMode());
            mWriter.printf("isUserForeground: %b\n",
                    mDevicePolicyManagerGateway.isUserForeground());
        }
    }

    private void showUsage() {
        mWriter.printf("Usage:\n\n");
        mWriter.printf("\t%s - show this help\n", CMD_HELP);
        mWriter.printf("\t%s - dump internal state\n", CMD_DUMP);
        mWriter.printf("\t%s [%s FLAGS] [NAME] - create a user with the optional flags and name\n",
                CMD_CREATE_USER, ARG_FLAGS);
        File setIconRootDir = UserIconContentProvider.getStorageDirectory(mContext);
        mWriter.printf("\t%s <FILE> - sets the user icon using the bitmap located at the given "
                + "file,\n"
                + "\t\twhich must be located in the user's `%s` directory.\n"
                + "\t\tFor user 0, you can use `adb push` to push a local file to that directory\n"
                + "\t\t(%s),\n"
                + "\t\tbut for other users you need to switch to that user and use its content "
                + "provider \n"
                + "\t\t(for example, `adb shell content write --user 10 --uri \n"
                + "\t\tcontent://%s/icon.png < /tmp/icon.png`)\n", CMD_SET_USER_ICON,
                setIconRootDir.getName(), setIconRootDir, UserIconContentProvider.AUTHORITY);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - remove the given user\n", CMD_REMOVE_USER);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - switch the given user to foreground\n",
                CMD_SWITCH_USER);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - start the given user in the background\n",
                CMD_START_USER_BG);
        mWriter.printf("\t%s - checks if the user is affiliated with the device\n",
                CMD_IS_USER_AFFILIATED);
        mWriter.printf("\t%s [ID1] [ID2] [IDN] - sets the user affiliation ids (or clear them if "
                + "no ids is passed)\n", CMD_SET_AFFILIATION_IDS);
        mWriter.printf("\t%s - gets the user affiliation ids\n",
                CMD_GET_AFFILIATION_IDS);
        mWriter.printf("\t%s - list the user restrictions\n", CMD_LIST_USER_RESTRICTIONS);
        mWriter.printf("\t%s <RESTRICTION> <true|false>- set the given user restriction\n",
                CMD_SET_USER_RESTRICTION);
        mWriter.printf("\t%s [FLAGS]- lock the device (now! :-)\n", CMD_LOCK_NOW);
        mWriter.printf("\t%s - reboot the device\n", CMD_REBOOT);
        mWriter.printf("\t%s [FLAGS]- factory reset the device\n", CMD_WIPE_DATA);
        mWriter.printf("\t%s - request a bugreport\n", CMD_REQUEST_BUGREPORT);
        mWriter.printf("\t%s <true|false> - enable / disable network logging\n",
                CMD_SET_NETWORK_LOGGING);
        mWriter.printf("\t%s [NAME] - set the organization name; use it without a name to reset\n",
                CMD_SET_ORGANIZATION_NAME);
        mWriter.printf("\t%s - get the organization name\n", CMD_GET_ORGANIZATION_NAME);
        mWriter.printf("\t%s [PKG1] [PKG2] [PKGN] - sets the packages that the user cannot\n"
                + "\t\tforce stop or clear data. Use no args to reset it.\n",
                CMD_SET_USER_CONTROL_DISABLED_PACKAGES);
        mWriter.printf("\t%s - gets the packages that the user cannot force stop or "
                + "clear data\n", CMD_GET_USER_CONTROL_DISABLED_PACKAGES);
        mWriter.printf("\t%s - remove itself as an admin\n", CMD_REMOVE_ACTIVE_ADMIN);
        mWriter.printf("\t%s - clear itself as device owner \n", CMD_CLEAR_DEVICE_OWNER);
        mWriter.printf("\t%s - clear itself as profile owner \n", CMD_CLEAR_PROFILE_OWNER);
        mWriter.printf("\t%s <QUALITY> - set password quality\n",
                CMD_SET_PASSWORD_QUALITY);
        mWriter.printf("\t%s - get password quality\n", CMD_GET_PASSWORD_QUALITY);
        mWriter.printf("\t%s [ADMIN]- transfer ownership to the given admin\n",
                CMD_TRANSFER_OWNERSHIP);
        mWriter.printf("\t%s <SUSPENDED> <PKG1> [PKG2] [PGKN] - suspend / unsuspend the given "
                + "packages\n", CMD_SET_SUSPENDED_PACKAGES);
        mWriter.printf("\t%s <PKG1> [PKG2] [PKGN] - checks if the given packages are suspended\n",
                CMD_IS_SUSPENDED_PACKAGE);
        mWriter.printf("\t%s <SUSPENDED> - suspend / unsuspend personal apps\n",
                CMD_SET_PERSONAL_APPS_SUSPENDED);
        mWriter.printf("\t%s - enable the given system app \n", CMD_ENABLE_SYSTEM_APP);
        mWriter.printf("\t%s - list the disabled system apps \n", CMD_LIST_DISABLED_SYSTEM_APPS);
        mWriter.printf("\t%s - gets the reasons for suspending personal apps\n",
                CMD_GET_PERSONAL_APPS_SUSPENDED_REASONS);
        mWriter.printf("\t%s <PKG> <HIDDEN> - hide / unhide the given package\n",
                CMD_SET_HIDDEN_PACKAGE);
        mWriter.printf("\t%s <PKG> - checks if the given package is hidden\n",
                CMD_IS_HIDDEN_PACKAGE);
        mWriter.printf("\t%s <PKG1> [PKG2] [PGKN] - set the packages allowed to have tasks locked"
                + "\n", CMD_SET_LOCK_TASK_PACKAGES);
        mWriter.printf("\t%s - get the packages allowed to have tasks locked\n",
                CMD_GET_LOCK_TASK_PACKAGES);
        mWriter.printf("\t%s <PKG1> [PKG2] [PKGN] - checks if the given packages are allowed to "
                + "have tasks locked\n", CMD_IS_LOCK_TASK_PERMITTED);
        mWriter.printf("\t%s <FLAGS> - set the lock task features\n", CMD_SET_LOCK_TASK_FEATURES);
        mWriter.printf("\t%s - get the lock task features\n", CMD_GET_LOCK_TASK_FEATURES);
        mWriter.printf("\t%s <PKG> [K1 V1] [Kn Vn] - sets the key/value (as String) application "
                + "restrictions for the given app (or resets when no key/value is passed)\n",
                CMD_SET_APP_RESTRICTIONS);
        mWriter.printf("\t%s [PKG1] [PKGNn] - get the application restrictions for the given apps, "
                + "or for TestDPC itself (using UserManager) when PKG is not passed\n",
                CMD_GET_APP_RESTRICTIONS);
        mWriter.printf("\t%s <PKG> <PERMISSION> <STATE> - set the grant state for the given "
                + "package / permission\n",
                CMD_SET_PERMISSION_GRANT_STATE);
        mWriter.printf("\t%s <PKG> <PERMISSION> - get the grant state for the given permission / "
                + "package\n", CMD_GET_PERMISSION_GRANT_STATE);
        mWriter.printf("\t%s <true|false> - set location enabled for the user\n",
                CMD_SET_LOCATION_ENABLED);
        mWriter.printf("\t%s - get whether location is enabled for the user\n",
                CMD_IS_LOCATION_ENABLED);
        mWriter.printf("\t%s [INFO] - set the device owner lock screen info (or reset when no INFO "
                + "is passed)\n", CMD_SET_DEVICE_OWNER_LOCKSCREEN_INFO);
        mWriter.printf("\t%s - get the device owner lock screen info\n",
                CMD_GET_DEVICE_OWNER_LOCKSCREEN_INFO);
        mWriter.printf("\t%s <true|false> - set keyguard disabled\n", CMD_SET_KEYGUARD_DISABLED);
        mWriter.printf("\t%s <FLAGS> - set the keyguard disabled features\n",
                CMD_SET_KEYGUARD_DISABLED_FEATURES);
        mWriter.printf("\t%s - get the keyguard disabled features\n",
                CMD_GET_KEYGUARD_DISABLED_FEATURES);

        // Separator for S / pre-S commands - do NOT remove line to avoid cherry-pick conflicts

        if (Util.isAtLeastS()) {
            mWriter.printf("\t%s <true|false> - enable / disable USB data signaling\n",
                    CMD_SET_USB_DATA_SIGNALING_ENABLED);
            mWriter.printf("\t%s [MET1] <MET2> <METN>- set the permitted input methods in the "
                    + "parent's device admin\n",
                    CMD_SET_PERMITTED_INPUT_METHODS_PARENT);
            mWriter.printf("\t%s - list the users running on foreground\n",
                    CMD_LIST_FOREGROUND_USERS);
            mWriter.printf("\t%s - checks if the calling user is running on foreground\n",
                    CMD_IS_FOREGROUND_USER);
        }
    }

    private void createUser() {
        // TODO(b/171350084): once more commands are added, add a generic argument parsing
        // mechanism like getRequiredArg(), getOptionalArg, etc...
        int nextArgIndex = 1;
        String nextArg = null;

        final String name;
        int flags = 0;

        if (mArgs.length > nextArgIndex) {
            nextArg = mArgs[nextArgIndex++];
            if (ARG_FLAGS.equals(nextArg)) {
                flags = Integer.parseInt(mArgs[nextArgIndex++]);
                if (mArgs.length > nextArgIndex) {
                    name = mArgs[nextArgIndex++];
                }
                else {
                    name = null;
                }
            } else {
                name = nextArg;
            }
        } else {
            name = null;
        }
        Log.i(TAG, "createUser(): name=" + name + ", flags=" + flags);

        mDevicePolicyManagerGateway.createAndManageUser(name, flags,
                (u) -> onSuccess("User created: %s", toString(u)),
                (e) -> onError(e, "Error creating user %s", name));
    }

    private void setUserIcon() {
        if (!hasExactlyNumberOfArguments(2)) return;

        String name = mArgs[1];
        Log.i(TAG, "setUserIcon(): name=" + name);

        File file = UserIconContentProvider.getFile(mContext, name);

        if (!file.isFile()) {
            mWriter.printf("Could not open file %s\n", name);
            return;
        }

        String absolutePath = file.getAbsolutePath();
        Log.i(TAG, "setUserIcon(): path=" + absolutePath);
        Bitmap icon = BitmapFactory.decodeFile(absolutePath, /* bmOptions= */ null);
        if (icon == null) {
            mWriter.printf("Could not create bitmap from file %s\n", absolutePath);
            return;
        }
        mDevicePolicyManagerGateway.setUserIcon(icon,
                (v) -> onSuccess("User icon created from file %s", absolutePath),
                (e) -> onError(e, "Error creating user icon from file %s", absolutePath));
    }

    private void removeUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.removeUser(userHandle,
                (v) -> onSuccess("User %s removed", userHandle),
                (e) -> onError(e, "Error removing user %s", userHandle));
    }

    private void switchUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.switchUser(userHandle,
                (v) -> onSuccess("User %s switched", userHandle),
                (e) -> onError(e, "Error switching user %s", userHandle));
    }

    private void startUserInBackground() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.startUserInBackground(userHandle,
                (v) -> onSuccess("User %s started in background", userHandle),
                (e) -> onError(e, "Error starting user %s in background", userHandle));
    }

    private void stopUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.stopUser(userHandle,
                (v) -> onSuccess("User %s stopped", userHandle),
                (e) -> onError(e, "Error stopping user %s", userHandle));
    }

    private void getAffiliationIds() {
        Set<String> ids = mDevicePolicyManagerGateway.getAffiliationIds();
        if (ids.isEmpty()) {
            mWriter.println("no affiliation ids");
            return;
        }
        mWriter.printf("%d affiliation ids: %s\n", ids.size(), ids);
    }

    private void setAffiliationIds() {
        Set<String> ids = getSetFromAllArgs();
        Log.i(TAG, "setAffiliationIds(): ids=" + ids);
        mDevicePolicyManagerGateway.setAffiliationIds(ids);

        getAffiliationIds();
    }

    private void isUserAffiliated() {
        mWriter.println(mDevicePolicyManagerGateway.isAffiliatedUser());
    }


    private void listUserRestrictions() {
        Log.i(TAG, "listUserRestrictions()");

        printCollection("user restriction", mDevicePolicyManagerGateway.getUserRestrictions());
    }

    private void setUserRestriction() {
        // TODO(b/171350084): check args
        String userRestriction = mArgs[1];
        boolean enabled = Boolean.parseBoolean(mArgs[2]);
        Log.i(TAG, "setUserRestriction(" + userRestriction + ", " + enabled + ")");

        mDevicePolicyManagerGateway.setUserRestriction(userRestriction, enabled,
                (v) -> onSuccess("User restriction '%s' set to %b", userRestriction, enabled),
                (e) -> onError(e, "Error setting user restriction '%s' to %b", userRestriction,
                        enabled));
    }

    private void lockNow() {
        Integer flags = getIntArg(/* index= */ 1);
        if (flags == null) {
            Log.i(TAG, "lockNow()");
            mDevicePolicyManagerGateway.lockNow(
                    (v) -> onSuccess("Device locked"),
                    (e) -> onError(e, "Error locking device"));
        } else {
            Log.i(TAG, "lockNow(" + flags + ")");
            mDevicePolicyManagerGateway.lockNow(flags,
                    (v) -> onSuccess("Device locked"),
                    (e) -> onError(e, "Error locking device"));
        }
    }

    private void reboot() {
        Log.i(TAG, "reboot()");
        mDevicePolicyManagerGateway.reboot(
                (v) -> onSuccess("Device rebooted"),
                (e) -> onError(e, "Error rebooting device"));
    }

    private void wipeData() {
        Integer flags = getIntArg(/* index= */ 1);
        Log.i(TAG, "wipeData()");
        mDevicePolicyManagerGateway.wipeData(flags == null ? 0 : flags,
                (v) -> onSuccess("Data wiped"),
                (e) -> onError(e, "Error wiping data"));
    }

    private void requestBugreport() {
        Log.i(TAG, "requestBugreport()");
        mDevicePolicyManagerGateway.requestBugreport(
                (v) -> onSuccess("Bugreport requested"),
                (e) -> onError(e, "Error requesting bugreport"));
    }

    private void setNetworkLogging() {
        // TODO(b/171350084): check args
        boolean enabled = Boolean.parseBoolean(mArgs[1]);
        Log.i(TAG, "setNetworkLogging(" + enabled + ")");

        mDevicePolicyManagerGateway.setNetworkLogging(enabled,
                (v) -> onSuccess("Network logging set to %b", enabled),
                (e) -> onError(e, "Error setting network logging to %b", enabled));
    }

    private void setOrganizationName() {
        String title = mArgs.length > 1 ? mArgs[1] : null;
        Log.i(TAG, "setOrganizationName(" + title + ")");

        mDevicePolicyManagerGateway.setOrganizationName(title,
                (v) -> onSuccess("Organization name set to %s", title),
                (e) -> onError(e, "Error setting Organization name to %s", title));
    }

    private void getOrganizationName() {
        CharSequence title = mDevicePolicyManagerGateway.getOrganizationName();
        if (title == null) {
            mWriter.println("Not set");
            return;
        }
        mWriter.println(title);
    }

    private void setUserControlDisabledPackages() {
        List<String> pkgs = getListFromAllArgs();
        Log.i(TAG, "setUserControlDisabledPackages(" + pkgs + ")");

        mDevicePolicyManagerGateway.setUserControlDisabledPackages(pkgs,
                (v) -> onSuccess("User-control disabled packages set to %s", pkgs),
                (e) -> onError(e, "Error setting User-control disabled packages to %s", pkgs));
    }

    private void getUserControlDisabledPackages() {
        List<String> pkgs = mDevicePolicyManagerGateway.getUserControlDisabledPackages();
        pkgs.forEach((p) -> mWriter.println(p));
    }

    private void removeActiveAdmin() {
        Log.i(TAG, "removeActiveAdmin()");

        ComponentName admin = mDevicePolicyManagerGateway.getAdmin();
        mDevicePolicyManagerGateway.removeActiveAdmin(
                (v) -> onSuccess("Removed %s as an active admin", admin),
                (e) -> onError(e, "Error removing %s as admin", admin));
    }

    private void clearDeviceOwner() {
        Log.i(TAG, "clearDeviceOwner()");

        String pkg = mDevicePolicyManagerGateway.getAdmin().getPackageName();
        mDevicePolicyManagerGateway.clearDeviceOwnerApp(
                (v) -> onSuccess("Removed %s as device owner", pkg),
                (e) -> onError(e, "Error removing %s as device owner", pkg));
    }

    private void clearProfileOwner() {
        Log.i(TAG, "clearProfileOwner()");

        String pkg = mDevicePolicyManagerGateway.getAdmin().getPackageName();
        mDevicePolicyManagerGateway.clearProfileOwner(
                (v) -> onSuccess("Removed %s as profile owner", pkg),
                (e) -> onError(e, "Error removing %s as profile owner", pkg));
    }

    private void setPermittedInputMethodsOnParent() {
        List<String> inputMethods = getListFromAllArgs();
        Log.i(TAG, "setPermittedInputMethodsOnParent(" + inputMethods + ")");

        DevicePolicyManagerGateway parentDpmGateway =
            DevicePolicyManagerGatewayImpl.forParentProfile(mContext);
        parentDpmGateway.setPermittedInputMethods(inputMethods);
    }

    private void listForegroundUsers() {
        List<UserHandle> users = mDevicePolicyManagerGateway.listForegroundAffiliatedUsers();
        if (users.isEmpty()) {
            mWriter.println("none");
            return;
        }
        int size = users.size();
        mWriter.printf("%d user%s:\n", size, (size > 1 ? "s" : ""));
        users.forEach(u -> mWriter.printf("\t%s\n", u));
    }

    private void isForegroundUser() {
        mWriter.println(mDevicePolicyManagerGateway.isUserForeground());
    }

    private void setPasswordQuality() {
        int quality = getIntArg(/* index= */ 1);
        Log.i(TAG, "setPasswordQuality(" + quality + ")");

        mDevicePolicyManagerGateway.setPasswordQuality(quality,
                (v) -> onSuccess("Set password quality to %d", quality),
                (e) -> onError(e, "Error setting password quality to %d", quality));
    }

    private void getPasswordQuality() {
        mWriter.printf("password quality: %d\n", mDevicePolicyManagerGateway.getPasswordQuality());
    }

    private void transferOwnership() {
        // TODO(b/171350084): check args
        String flatTarget = mArgs[1];
        ComponentName target = ComponentName.unflattenFromString(flatTarget);

        Log.i(TAG, "transferOwnership(" + target + ")");

        mDevicePolicyManagerGateway.transferOwnership(target, /* bundle= */ null,
            (v) -> onSuccess("Ownership transferred to %s", flatTarget),
            (e) -> onError(e, "Error transferring ownership to %s", flatTarget));
    }

    private void setUsbDataSignalingEnabled() {
        boolean enabled = Boolean.parseBoolean(mArgs[1]);
        Log.i(TAG, "setUsbDataSignalingEnabled(" + enabled + ")");

        mDevicePolicyManagerGateway.setUsbDataSignalingEnabled(enabled,
            (v) -> onSuccess("USB data signaling set to %b", enabled),
            (e) -> onError(e, "Error setting USB data signaling to %b", enabled));
    }

    private static String suspendedToString(boolean suspended) {
        return suspended ? "SUSPENDED" : "NOT SUSPENDED";
    }

    private void setSuspendedPackages() {
        boolean suspended = Boolean.parseBoolean(mArgs[1]);
        String[] packageNames = getArrayFromArgs(/* index= */ 2);

        String printableNames = Arrays.toString(packageNames);
        String printableStatus = suspendedToString(suspended);

        Log.i(TAG, "setSuspendedPackages(" + printableNames + "): " + printableStatus);

        mDevicePolicyManagerGateway.setPackagesSuspended(packageNames, suspended,
            (v) -> onSuccess("Set %s (but not %s) to %s", printableNames, Arrays.toString(v),
                    printableStatus),
            (e) -> onError(e, "Error settings %s to %s", printableNames, printableStatus));
    }

    private void isSuspendedPackage() {
        getListFromAllArgs().forEach((packageName) -> {
            try {
                boolean suspended = mDevicePolicyManagerGateway.isPackageSuspended(packageName);
                mWriter.printf("%s: %s\n", packageName, suspendedToString(suspended));
            } catch (NameNotFoundException e) {
                mWriter.printf("Invalid package name: %s\n", packageName);
            }
        });
    }

    private static String hiddenToString(boolean hidden) {
        return hidden ? "HIDDEN" : "VISIBLE";
    }

    private void setHiddenPackage() {
        // TODO(b/171350084): check args
        String packageName = mArgs[1];
        boolean hidden = Boolean.parseBoolean(mArgs[2]);
        String printableStatus = hiddenToString(hidden);

        Log.i(TAG, "setHiddenPackages(" + packageName + "): " + printableStatus);
        mDevicePolicyManagerGateway.setApplicationHidden(packageName, hidden,
            (v) -> onSuccess("Set %s as %s", packageName, printableStatus),
            (e) -> onError(e, "Error settings %s as %s", packageName, printableStatus));
    }

    private void isHiddenPackage() {
        // TODO(b/171350084): check args
        String packageName = mArgs[1];
        try {
            boolean hidden = mDevicePolicyManagerGateway.isApplicationHidden(packageName);
            mWriter.printf("%s: %s\n", packageName, hiddenToString(hidden));
        } catch (NameNotFoundException e) {
            mWriter.printf("Invalid package name: %s\n", packageName);
        }
    }

    private void setPersonalAppsSuspended() {
        boolean suspended = Boolean.parseBoolean(mArgs[1]);
        String printableStatus = suspendedToString(suspended);

        Log.i(TAG, "setPersonalAppsSuspended(): " + printableStatus);

        mDevicePolicyManagerGateway.setPersonalAppsSuspended(suspended,
            (v) -> onSuccess("Set personal apps to %s", printableStatus),
            (e) -> onError(e, "Error setting personal apps to %s", printableStatus));
    }

    private void getPersonalAppsSuspendedReasons() {
        int reasons = mDevicePolicyManagerGateway.getPersonalAppsSuspendedReasons();
        String printableReasons = Util.personalAppsSuspensionReasonToString(reasons);

        mWriter.printf("%s (%d)\n", printableReasons, reasons);
    }

    private void enableSystemApp() {
        // TODO(b/171350084): check args
        String packageName = mArgs[1];

        Log.i(TAG, "enableSystemApp(): " + packageName);

        mDevicePolicyManagerGateway.enableSystemApp(packageName,
            (v) -> onSuccess("Enabled system apps %s", packageName),
            (e) -> onError(e, "Error enabling systen app%s", packageName));
    }

    private void listDisabledSystemApps() {
        List<String> disabledSystemApps = mDevicePolicyManagerGateway.getDisabledSystemApps();

        Log.i(TAG, "listDisabledSystemApps(): " + disabledSystemApps);

        printCollection("disabled system app", disabledSystemApps);
    }

    private void setLockTaskPackages() {
        String[] packages = getArrayFromArgs(/* index= */ 1);

        String printableNames = Arrays.toString(packages);

        Log.i(TAG, "setLockTaskPackages(): " + printableNames);

        mDevicePolicyManagerGateway.setLockTaskPackages(packages,
            (v) -> onSuccess("Set lock tasks packages to %s", printableNames),
            (e) -> onError(e, "Error settings lock task packages to %s", printableNames));
    }

    private void getLockTaskPackages() {
        String[] packages = mDevicePolicyManagerGateway.getLockTaskPackages();
        if (packages.length == 0) {
            mWriter.println("no lock task packages");
            return;
        }
        mWriter.println(Arrays.toString(packages));
    }

    private void setLockTaskFeatures() {
        int flags = getIntArg(/* index= */ 1);

        String features = Util.lockTaskFeaturesToString(flags);
        Log.i(TAG, "setLockTaskFeatures(" + flags + "): setting to " + features);

        mDevicePolicyManagerGateway.setLockTaskFeatures(flags,
                (v) -> onSuccess("Set lock tasks features to %s", features),
                (e) -> onError(e, "Error settings lock task features to %s", features));
    }

    private void getLockTaskFeatures() {
        int flags = mDevicePolicyManagerGateway.getLockTaskFeatures();
        String features = Util.lockTaskFeaturesToString(flags);

        mWriter.printf("%s (%d)\n", features, flags);
    }

    private void setAppRestrictions() {
        // TODO(b/171350084): check args size
        String packageName = mArgs[1];
        Bundle settings = new Bundle();
        for (int i = 2; i < mArgs.length; i++) {
            String key = mArgs[i];
            String value = mArgs[++i];
            settings.putString(key, value);
        }
        mDevicePolicyManagerGateway.setApplicationRestrictions(packageName, settings,
                (v) -> onSuccess("Set %d app restrictions for %s", settings.size(), packageName),
                (e) -> onError(e, "Error setting app restrictions for %s", packageName));
    }

    private void getAppRestrictions() {
        if (mArgs.length == 1) {
            printAppRestrictions(mContext.getPackageName(),
                    mDevicePolicyManagerGateway.getSelfRestrictions());
            return;
        }

        for (int i = 1; i < mArgs.length; i++) {
            String packageName = mArgs[i];
            Bundle settings = mDevicePolicyManagerGateway.getApplicationRestrictions(packageName);
            printAppRestrictions(packageName, settings);
        }
    }

    private void printAppRestrictions(String packageName, Bundle settings) {
        if (settings == null || settings.isEmpty()) {
            mWriter.printf("No app restrictions for %s\n", packageName);
            return;
        }
        mWriter.printf("%d app restrictions for %s\n", settings.size(), packageName);
        for (String key : settings.keySet()) {
            Object value = settings.get(key);
            mWriter.printf("  %s = %s\n", key, value);
        }
    }

    private void setPermissionGrantState() {
        // TODO(b/171350084): check args
        String packageName = mArgs[1];
        String permission = mArgs[2];
        int grantState = getIntArg(3);
        String grantName = Util.grantStateToString(grantState);

        mDevicePolicyManagerGateway.setPermissionGrantState(packageName, permission, grantState,
                (v) -> onSuccess("Set %s state on %s to %s", permission, packageName, grantName),
                (e) -> onError(e, "Error setting %s state on %s to %s", packageName, permission,
                        grantName));
    }

    private void getPermissionGrantState() {
        // TODO(b/171350084): check args
        String packageName = mArgs[1];
        String permission = mArgs[2];
        int grantState = mDevicePolicyManagerGateway.getPermissionGrantState(packageName,
                permission);
        mWriter.printf("%s state for %s: %s\n", permission, packageName,
                Util.grantStateToString(grantState));
    }

    private void setLocationEnabled() {
        // TODO(b/171350084): check args
        boolean enabled = Boolean.parseBoolean(mArgs[1]);

        mDevicePolicyManagerGateway.setLocationEnabled(enabled,
                (v) -> onSuccess("Set location enabled to %b", enabled),
                (e) -> onError(e, "Error setting location enabled to %b", enabled));
    }

    private void isLocationEnabled() {
        boolean enabled = mDevicePolicyManagerGateway.isLocationEnabled();
        mWriter.printf("Location enabled: %b\n", enabled);
    }

    private void setDeviceOwnerLockScreenInfo() {
        final CharSequence info = mArgs.length > 0 ? mArgs[1] : "";
        mDevicePolicyManagerGateway.setDeviceOwnerLockScreenInfo(info,
                (v) -> onSuccess("Set lock screen info to '%s'", info),
                (e) -> onError(e, "Error setting lock screen info to '%s'", info));
    }

    private void getDeviceOwnerLockScreenInfo() {
        CharSequence info = mDevicePolicyManagerGateway.getDeviceOwnerLockScreenInfo();
        mWriter.printf("Lock screen info: %s\n", info);
    }
    
    private void setKeyguardDisabled() {
        // TODO(b/171350084): check args
        boolean disabled = Boolean.parseBoolean(mArgs[1]);

        mDevicePolicyManagerGateway.setKeyguardDisabled(disabled,
                (v) -> onSuccess("Set keyguard disabled to %b", disabled),
                (e) -> onError(e, "Error setting keyguard disabled to %b", disabled));
    }

    private void setKeyguardDisabledFeatures() {
        int which = getIntArg(/* index= */ 1);

        String features = Util.keyguardDisabledFeaturesToString(which);
        Log.i(TAG, "setKeyguardDisabledFeatures(" + which + "): setting to " + features);

        mDevicePolicyManagerGateway.setKeyguardDisabledFeatures(which,
                (v) -> onSuccess("Set keyguard features to %s", features),
                (e) -> onError(e, "Error settings keyguard features to %s", features));
    }

    private void getKeyguardDisabledFeatures() {
        int flags = mDevicePolicyManagerGateway.getKeyguardDisabledFeatures();
        String features = Util.keyguardDisabledFeaturesToString(flags);

        mWriter.printf("%s (%d)\n", features, flags);
    }

    private static String permittedToString(boolean permitted) {
        return permitted ? "PERMITTED" : "NOT PERMITTED";
    }

    private void isLockTaskPermitted() {
        getListFromAllArgs().forEach((packageName) -> {
            boolean permitted = mDevicePolicyManagerGateway.isLockTaskPermitted(packageName);
            mWriter.printf("%s: %s\n", packageName, permittedToString(permitted));
        });
    }

    private void execute(@NonNull Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            // Must explicitly catch and show generic exceptions (like NumberFormatException parsing
            // args), otherwise they'dbe logcat'ed on AndroidRuntime and not surfaced to caller
            onError(e, "error executing %s", Arrays.toString(mArgs));
        }
    }

    private void onSuccess(@NonNull String pattern, @Nullable Object...args) {
        String msg = String.format(pattern, args);
        Log.d(TAG, msg);
        mWriter.println(msg);
    }

    private void onError(@NonNull Exception e, @NonNull String pattern, @Nullable Object...args) {
        String msg = String.format(pattern, args);
        Log.e(TAG, msg, e);
        mWriter.printf("%s: %s\n", msg, e);
    }

    private void printCollection(String nameOnSingular, Collection<String> collection) {
        if (collection.isEmpty()) {
            mWriter.printf("No %ss\n", nameOnSingular);
            return;

        }
        int size = collection.size();
        mWriter.printf("%d %s%s:\n", size, nameOnSingular, size == 1 ? "" : "s");
        collection.forEach((s) -> mWriter.printf("  %s\n", s));
    }

    private String toString(UserHandle user) {
        return user.toString() + " serial=" + mDevicePolicyManagerGateway.getSerialNumber(user);
    }

    private UserHandle getUserHandleArg(int index) {
        // TODO(b/171350084): check args
        long serialNumber = Long.parseLong(mArgs[index]);
        UserHandle userHandle = mDevicePolicyManagerGateway.getUserHandle(serialNumber);
        if (userHandle == null) {
            mWriter.printf("No user handle for serial number %d\n", serialNumber);
        }
        return userHandle;
    }

    /** Gets an optional {@code int} argument at index {@code index}. */
    @Nullable
    private Integer getIntArg(int index) {
        return mArgs.length <= index ? null : Integer.parseInt(mArgs[index]);
    }

    private boolean hasExactlyNumberOfArguments(int number) {
        if (mArgs.length != number) {
            mWriter.printf("Must have exactly %d arguments: %s\n", number, Arrays.toString(mArgs));
            return false;
        }
        return true;
    }

    private List<String> getListFromAllArgs() {
        List<String> list = new ArrayList<>(mArgs.length - 1);
        for (int i = 1; i < mArgs.length; i++) {
            list.add(mArgs[i]);
        }
        return list;
    }

    private Set<String> getSetFromAllArgs() {
        return new LinkedHashSet<String>(getListFromAllArgs());
    }

    private String[] getArrayFromArgs(int startingIndex) {
        int size = mArgs.length - startingIndex;
        String[] array = new String[size];
        System.arraycopy(mArgs, startingIndex, array, 0, size);
        return array;
    }
}
