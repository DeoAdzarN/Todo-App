package com.deo.todo_app.view.bottomSheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.deo.todo_app.R
import com.deo.todo_app.data.local.database.AppDatabase
import com.deo.todo_app.data.repository.AttachmentRepository
import com.deo.todo_app.data.repository.TaskRepository
import com.deo.todo_app.databinding.BsUpsertTaskBinding
import com.deo.todo_app.helper.DateTimeHelper
import com.deo.todo_app.helper.FirebaseStorageHelper
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Task
import com.deo.todo_app.utils.Connectivity.isInternetAvailable
import com.deo.todo_app.utils.TaskReminders
import com.deo.todo_app.utils.TaskStatus
import com.deo.todo_app.view.adapter.AttachmentAdapter
import com.deo.todo_app.view.dialog.CustomDialog
import com.deo.todo_app.view.dialog.CustomDialog.showImageDialog
import com.deo.todo_app.viewModel.AttachmentViewModel
import com.deo.todo_app.viewModel.TaskViewModel
import com.deo.todo_app.viewModel.factory.AttachmentViewModelFactory
import com.deo.todo_app.viewModel.factory.TaskViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpsertTaskBottomSheet(private val context: Context, private val task: Task?,private val onDissmiss:() -> Unit) : BottomSheetDialogFragment() {
    private lateinit var _binding: BsUpsertTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var attachmentViewModel: AttachmentViewModel
    private lateinit var pickMediaLauncher: ActivityResultLauncher<Intent>
    private var currentUri: Uri? = null
    private var listAttachment: MutableList<Attachment> = mutableListOf()
    private var listAttachmentRemoveId: MutableList<String> = mutableListOf()
    private lateinit var attachmentAdapter: AttachmentAdapter
    private val progressDialog = CustomDialog.ProgressDialogFragment()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.RoundedBottomSheetDialog)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BsUpsertTaskBinding.inflate(inflater, container, false)

        val taskRepository = TaskRepository(
            taskDao = AppDatabase.getInstance(context).taskDao(),
            firestore = Firebase.firestore
        )
        val attachmentRepository = AttachmentRepository(
            attachmentDao = AppDatabase.getInstance(context).attachmentDao(),
            firestore = Firebase.firestore
        )
        val factory = TaskViewModelFactory(taskRepository)
        val attachmentFactory = AttachmentViewModelFactory(attachmentRepository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
        attachmentViewModel = ViewModelProvider(this, attachmentFactory)[AttachmentViewModel::class.java]
        attachmentAdapter = AttachmentAdapter(context, listAttachment, { attachment, position ->
            //onRemove
            if (task!=null){
                listAttachmentRemoveId.add(attachment.id)
            }
            listAttachment.removeAt(position)
            attachmentAdapter.notifyItemRemoved(position)
        }, { attachment ->
            //onClickListener
            showImageDialog(context, attachment.url, attachment.path)
        })
        _binding.rvAttachment.adapter = attachmentAdapter
        val listSpinner = resources.getStringArray(R.array.status)
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listSpinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setupPickMediaLauncher()

        _binding.apply {
            statusSpinner.adapter = spinnerAdapter
            dateContainer.setOnClickListener {
                showDateTimePickerDialog()
            }
            attachmentContainer.setOnClickListener {
                showPhotoVideoDialog(onPhotoSelected = {
                    if (checkAndRequestPermissions()) {
                        openGallery("image/*")
                    }
                }, onVideoSelected = {
                    if (checkAndRequestPermissions()) {
                        openGallery("video/*")
                    }
                })
            }

            statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> statusSelector.setCardBackgroundColor(context.getColor(R.color.yellow))
                        1 -> statusSelector.setCardBackgroundColor(context.getColor(R.color.light_blue))
                        else -> statusSelector.setCardBackgroundColor(context.getColor(R.color.green))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Optional: handle no selection
                }
            }
        }

        if (task != null) {
            attachmentViewModel.getAttachmentsByTaskId(taskId = task.id)  // Pastikan ini memanggil fungsi yang mengisi LiveData
            attachmentViewModel.attach.observe(viewLifecycleOwner) { attachments ->
                // Pastikan data yang diterima tidak null
                if (attachments != null) {
                    listAttachment.clear()  // Clear existing list
                    listAttachment.addAll(attachments)  // Add all new attachments
                    attachmentAdapter.notifyDataSetChanged()  // Notify adapter
                } else {
                    Log.e("Attachment", "No attachments found.")
                }
            }
            Log.e("listAttachment", listAttachment.toString())
            _binding.apply {
                title.setText(task.title)
                desc.setText(task.description)
                dateText.text = DateTimeHelper().convertToDateString(task.date)
                setReminder.setText(getMinutesDifference(task.date, task.reminder).toString())
                statusSpinner.setSelection(
                    when (task.status) {
                        "Pending" -> 0
                        "On Progress" -> 1
                        else -> 2
                    }
                )
                apply.setOnClickListener {
                    Log.e("update", "onCreateView: 1" )
                    progressDialog.show(parentFragmentManager, "progressDialog")
                    val updatedTask = task.copy(
                        title = title.text.toString(),
                        description = desc.text.toString(),
                        date = DateTimeHelper().convertToMillis(dateText.text.toString()) ?: task.date,
                        reminder = subtractMinutesToMillis(dateText.text.toString(), setReminder.text.toString().toInt())
                            ?: task.reminder,
                        status = when (statusSpinner.selectedItemPosition) {
                            0 -> "Pending"
                            1 -> "On Progress"
                            else -> "Completed"
                        },
                    )

                    if (!isInternetAvailable(requireActivity())){
                        Log.e("update", "onCreateView: 2" )
                        taskViewModel.updateTaskOffline(updatedTask)
                        if (listAttachment.isEmpty()) {
                            progressDialog.dismiss()
                            Log.e("update", "onCreateView: 3" )

                        } else {
                            Log.e("update", "onCreateView: 4" )
                            progressDialog.dismiss()
                            listAttachment.forEach { attachment ->
                                attachment.taskId = updatedTask.id
                            }
                            attachmentViewModel.insertAttachmentOffline(listAttachment)
                        }
                        TaskReminders.scheduleReminder(context, updatedTask)
                        dismiss()
                    }else {
                        taskViewModel.updateTask(
                            updatedTask,
                            onResult = { userId: String, taskId: String ->
                                if (listAttachmentRemoveId.isNotEmpty()) {
                                    attachmentViewModel.removeAttachmentByTaskId(
                                        listAttachmentRemoveId
                                    )
                                }
                                if (listAttachment.isEmpty()) {
                                    progressDialog.dismiss()
                                    TaskReminders.scheduleReminder(context, updatedTask)
                                    dismiss()
                                } else {
                                    listAttachment.forEach { attachment ->
                                        attachment.taskId = taskId
                                    }
                                    attachmentViewModel.insertAttachment(
                                        listAttachment,
                                        onResult = { success: Boolean, message: String? ->
                                            if (success) {
                                                progressDialog.dismiss()
                                                TaskReminders.scheduleReminder(context, updatedTask)
                                                dismiss()
                                            } else {
                                                progressDialog.dismiss()
                                                Toast.makeText(
                                                    context,
                                                    message.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                }
                            })
                    }
                }
            }
        } else {
            Log.e("insert", "onCreateView: 1" )
            _binding.apply {
                apply.setOnClickListener {
                    Log.e("insert", "onCreateView: 2" )
                    progressDialog.show(parentFragmentManager, "progressDialog")
                    val newTask = Task(
                        title = title.text.toString(),
                        description = desc.text.toString(),
                        date = if (DateTimeHelper().convertToMillis(dateText.text.toString()) != null) DateTimeHelper().convertToMillis(dateText.text.toString())!! else Date().time,
                        reminder = if (subtractMinutesToMillis(dateText.text.toString(), setReminder.text.toString().toInt()) != null) subtractMinutesToMillis(dateText.text.toString(), setReminder.text.toString().toInt())!! else Date().time,
                        status = when (statusSpinner.selectedItemPosition) {
                            0 -> "Pending"
                            1 -> "On Progress"
                            else -> "Completed"
                        }
                    )

                    if (!isInternetAvailable(requireActivity())){
                        Log.e("insert", "onCreateView: 3" )
                        taskViewModel.insertTaskOffline(newTask)
                        if (listAttachment.isEmpty()) {
                            Log.e("insert", "onCreateView: 4" )
                            progressDialog.dismiss()
                        } else {
                            progressDialog.dismiss()
                            Log.e("insert", "onCreateView: 5 ${newTask.id}" )
                            listAttachment.forEach { attachment ->
                                attachment.taskId = newTask.id
                            }
                            attachmentViewModel.insertAttachmentOffline(listAttachment)
                        }
                        TaskReminders.scheduleReminder(context, newTask)
                        dismiss()
                    }else {
                        taskViewModel.insertTask(
                            newTask,
                            onResult = { userId: String, taskId: String ->
                                Log.e("insert", "step 1")
                                if (listAttachment.isEmpty()) {
                                    TaskReminders.scheduleReminder(context, newTask)
                                    dismiss()
                                } else {
                                    listAttachment.forEach { attachment ->
                                        attachment.taskId = taskId
                                    }
                                    attachmentViewModel.insertAttachment(
                                        listAttachment,
                                        onResult = { success: Boolean, message: String? ->
                                            if (success) {
                                                progressDialog.dismiss()
                                                TaskReminders.scheduleReminder(context, newTask)
                                                dismiss()
                                            } else {
                                                progressDialog.dismiss()
                                                Toast.makeText(
                                                    context,
                                                    message.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                }
                            })
                    }
                }
            }
        }
        return _binding.root
    }

    private fun setupPickMediaLauncher() {
        pickMediaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                currentUri = result.data!!.data
                currentUri?.let { uri ->
                    val filePath = getRealPathFromUri(uri)
                    val type = getTypeFromUri(uri)
                    if (type != null) {
                        when {
                            type.startsWith("image/") -> {
                                addAttachmentToList(filePath, "image")
                            }
                            type.startsWith("video/") -> {
                                addAttachmentToList(filePath, "video")
                            }
                            else -> {
                                addAttachmentToList(filePath, "image")
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Media selection canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun addAttachmentToList(filePath: String, type: String) {
        val attachment = Attachment(taskId = "", type = type, url = "", path = filePath)
        listAttachment.add(attachment)
        attachmentAdapter.notifyDataSetChanged() // Refresh RecyclerView
    }

    private fun getTypeFromUri(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    @SuppressLint("IntentReset")
    private fun openGallery(type: String) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = type
        pickMediaLauncher.launch(intent)
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readImagesPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            )
            if (readImagesPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    101
                )
                false
            } else {
                true
            }
        } else {
            val readStoragePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
                false
            } else {
                true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permissions[0])) {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs storage permission to pick and save images. Please enable the permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val packageName = context.packageName
                val intent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun getRealPathFromUri(uri: Uri): String {
        var path = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
            if (index != -1) {
                path = it.getString(index)
            }
            it.close()
        }
        return path
    }


    private fun getMinutesDifference(timestamp1: Long, timestamp2: Long): Long {
        val diffInMillis = timestamp1 - timestamp2
        return diffInMillis / (1000 * 60)
    }

    private fun subtractMinutesToMillis(dateString: String, minutes: Int): Long? {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString) // Parse the date string
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.MINUTE, -minutes) // Subtract minutes
                calendar.timeInMillis // Return result in milliseconds
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showPhotoVideoDialog(onPhotoSelected: () -> Unit, onVideoSelected: () -> Unit) {
        val options = arrayOf("Image", "Video")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose One")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> onPhotoSelected()
                1 -> onVideoSelected()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showDateTimePickerDialog() {
        // Show Date Picker
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // After date is selected, show time picker
                showTimePickerDialog(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Set selected date and time in Calendar
                val selectedDateTime = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, hourOfDay, minute)
                }

                // Format the selected date and time into a string
                val format = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                val formattedDate = format.format(selectedDateTime.time)

                // Display the formatted date and the milliseconds
                _binding.dateText.text = formattedDate
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDissmiss()
    }
}