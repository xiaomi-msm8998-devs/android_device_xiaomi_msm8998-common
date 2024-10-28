#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

import extract_utils.tools

extract_utils.tools.DEFAULT_PATCHELF_VERSION = '0_17_2'

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixup_remove,
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

namespace_imports = [
    'device/xiaomi/msm8998-common',
    'hardware/qcom-caf/common/libqti-perfd-client',
    'hardware/qcom-caf/msm8998',
    'hardware/qcom-caf/wlan',
    'hardware/xiaomi',
    'vendor/qcom/opensource/dataservices',
]

def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None

lib_fixups: lib_fixups_user_type = {
    **lib_fixups,
    (
        'com.qualcomm.qti.dpm.api@1.0',
        'dirac_resource.so',
        'vendor.qti.imsrtpservice@3.0',
        'vendor.qti.hardware.qccsyshal@1.0',
        'vendor.qti.hardware.qccvndhal@1.0',
        'vendor.qti.hardware.tui_comm@1.0',
    ): lib_fixup_vendor_suffix,
    (
    ): lib_fixup_remove,
}

blob_fixups: blob_fixups_user_type = {
    'system_ext/lib64/lib-imscamera.so': blob_fixup()
        .add_needed('libgui_shim.so'),
    'vendor/etc/izat.conf': blob_fixup()
        .patch_file('gps/0001-gps-izat-Disable-slim_daemon.patch'),
    'vendor/lib/hw/camera.msm8998.so': blob_fixup()
        .binary_regex_replace(b'service.bootanim.exit', b'service.bootanim.zzzz')
        .remove_needed('android.hidl.base@1.0.so')
        .remove_needed('libminikin.so'),
    (
     'vendor/lib/lib_lowlight.so',
     'vendor/lib/libSonyIMX386PdafLibrary.so',
     'vendor/lib/libXMFD_AgeGender.so',
     'vendor/lib/libarcsoft_beautyshot.so',
     'vendor/lib/libarcsoft_beautyshot_image_algorithm.so',
     'vendor/lib/libarcsoft_beautyshot_video_algorithm.so',
     'vendor/lib/libarcsoft_dualcam_optical_zoom.so',
     'vendor/lib/libarcsoft_dualcam_optical_zoom_control.so',
     'vendor/lib/libarcsoft_dualcam_refocus.so',
     'vendor/lib/libmorpho_easy_hdr.so',
     'vendor/lib/libmorpho_hdr_checker.so',
    ): blob_fixup()
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    'vendor/lib/lib_lowlight_dxo.so': blob_fixup()
        .fix_soname()
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    'vendor/lib/libFaceGrade.so': blob_fixup()
        .remove_needed('libandroid.so')
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    'vendor/lib/libMiCameraHal.so': blob_fixup()
        .add_needed('libcamera_shim.so')
        .add_needed('libpiex_shim.so')
        .remove_needed('libft2.so')
        .remove_needed('libharfbuzz_ng.so')
        .remove_needed('libheif.so')
        .remove_needed('libicuuc.so')
        .remove_needed('libminikin.so'),
    'vendor/lib/libarcsoft_beauty_shot.so': blob_fixup()
        .remove_needed('libandroid.so')
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    'vendor/lib/libmmcamera_hdr_gb_lib.so': blob_fixup()
        .add_needed('liblog.so')
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    (
     'vendor/lib/libmmcamera_pdaf.so',
     'vendor/lib/libmmcamera_pdafcamif.so',
     'vendor/lib/libmmcamera_tintless_bg_pca_algo.so',
    ): blob_fixup()
        .add_needed('liblog.so'),
    'vendor/lib/libmmcamera2_sensor_modules.so': blob_fixup()
        .binary_regex_replace(b'/data/misc/camera/camera_lsc_caldata.txt', b'/data/vendor/camera/camera_lsc_calib.txt'),
    'vendor/lib/libmmcamera2_stats_modules.so': blob_fixup()
        .remove_needed('libandroid.so')
        .remove_needed('libgui.so'),
    'vendor/lib/libmpbase.so': blob_fixup()
        .remove_needed('libandroid.so')
        .replace_needed('libstdc++.so', 'libstdc++_vendor.so'),
    'vendor/lib64/libdlbdsservice.so': blob_fixup()
        .replace_needed('libstagefright_foundation.so', 'libstagefright_foundation-v33.so'),
    'vendor/lib64/libril-qc-hal-qmi.so': blob_fixup()
        .replace_needed('android.hardware.radio.config@1.0.so', 'android.hardware.radio.c_shim@1.0.so')
        .replace_needed('android.hardware.radio.config@1.1.so', 'android.hardware.radio.c_shim@1.1.so')
        .replace_needed('android.hardware.radio.config@1.2.so', 'android.hardware.radio.c_shim@1.2.so'),
}  # fmt: skip

module = ExtractUtilsModule(
    'msm8998-common',
    'xiaomi',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
