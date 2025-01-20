//package com.himangskalita.nimbus
//
//class Temp {
//
//    private fun checkAndRequestLocationPermission() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            onLocationPermissionGranted()
//        } else {
//            // Use registerForActivityResult to request permissions
//            val requestPermissionLauncher = registerForActivityResult(
//                ActivityResultContracts.RequestMultiplePermissions()
//            ) { permissions ->
//                when {
//                    permissions[Manifest.permission.FINE_LOCATION] == true || permissions[Manifest.permission.COARSE_LOCATION] == true -> {
//                        onLocationPermissionGranted()
//                    }
//                    else -> {
//                        if (!shouldShowRequestPermissionRationale(Manifest.permission.FINE_LOCATION)) {
//                            showSettingsDialog()
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Location permission denied",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                }
//            }
//
//            requestPermissionLauncher.launch(arrayOf(Manifest.permission.FINE_LOCATION, Manifest.permission.COARSE_LOCATION))
//        }
//    }
//}