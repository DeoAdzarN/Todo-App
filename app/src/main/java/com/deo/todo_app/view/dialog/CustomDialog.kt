package com.deo.todo_app.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.deo.todo_app.R
import java.io.File

object CustomDialog {
    fun showCustomEditNameDialog(
        context: Context,
        currentName: String,
        onNameChange: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_name, null)

        val editTextName = dialogView.findViewById<EditText>(R.id.inputName)
        val buttonSave = dialogView.findViewById<Button>(R.id.saveBtn)
        editTextName.setText(currentName)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        buttonSave.setOnClickListener {
            val newName = editTextName.text.toString().trim()
            if (newName.isNotEmpty()) {
                onNameChange(newName)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    fun showCustomChangePasswordDialog(context: Context, onPasswordChange: (String, String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)

        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPassword)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPassword)
        val saveButton = dialogView.findViewById<Button>(R.id.saveBtn)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val toggleCurrentPassword = dialogView.findViewById<ImageView>(R.id.visibilityCurrentPassword)
        val toggleNewPassword = dialogView.findViewById<ImageView>(R.id.visibilityNewPassword)

        toggleCurrentPassword.setOnClickListener {
            toggleVisibilityPassword(currentPasswordEditText, toggleCurrentPassword)
        }

        toggleNewPassword.setOnClickListener {
            toggleVisibilityPassword(newPasswordEditText, toggleNewPassword)
        }

        saveButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                onPasswordChange(currentPassword, newPassword)
                dialog.dismiss()
            } else {
                Toast.makeText(
                    context,
                    "Current password and new password cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun toggleVisibilityPassword(passwordEditText: EditText, toggleIcon: ImageView) {
        val currentTypeface = passwordEditText.typeface

        if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            toggleIcon.setImageResource(R.drawable.ic_visibility_off)
        }else{
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_visible_on)
        }

        passwordEditText.typeface = currentTypeface
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    fun showCustomLogoutDialog(
        context: Context,
        isLogout: (Boolean) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_logout, null)

        val negative = dialogView.findViewById<Button>(R.id.negative)
        val positive = dialogView.findViewById<Button>(R.id.positive)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        negative.setOnClickListener {
            isLogout(false)
            dialog.dismiss()
        }

        positive.setOnClickListener {
            isLogout(true)
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    class ProgressDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.custom_progress_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            return dialog
        }
    }
    fun showImageDialog(context: Context,imageUrl: String?,imagePath:String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_preview_image)

        val imageView: ImageView = dialog.findViewById(R.id.previewImageView)
        val closeButton: ImageButton = dialog.findViewById(R.id.closeButton)

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.background_placeholder)
            .error(
                Glide.with(context)
                    .load(File(imagePath))
                    .placeholder(R.drawable.background_placeholder)
                    .error(R.drawable.background_placeholder)
            )
            .into(imageView)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}