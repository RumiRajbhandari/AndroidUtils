package com.evolve

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.evolve.databinding.FragmentImageBinding
import com.evolve.rosiautils.PictureManager
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.TYPE_SUCCESS
import com.evolve.rosiautils.loadImage
import com.evolve.rosiautils.showToast
import kotlinx.android.synthetic.main.activity_image.*

class ImageFragment : Fragment() {
    lateinit var binding: FragmentImageBinding
    private lateinit var pictureManager: PictureManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_image, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pictureManager = PictureManager(this)
        pictureManager.hasPermission()
        binding.btnTakePhotoFragment.setOnClickListener {
            context?.let {
                if (pictureManager.hasPermission()) {
                    openCamera()
                }
            }
        }
    }

    private fun openCamera() {
        pictureManager.startCameraIntent(requireContext()) { imgPath ->
            loadImage(binding.imgTakePhoto, imgPath) {
                if (!it) {
                    showToast("something went wrong", TYPE_ERROR)
                } else
                    showToast("success", TYPE_SUCCESS)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("on request permission res")
        if (pictureManager.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        ) openCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("on activity result")
        pictureManager.onActivityResult(requestCode, resultCode, data)
    }

}