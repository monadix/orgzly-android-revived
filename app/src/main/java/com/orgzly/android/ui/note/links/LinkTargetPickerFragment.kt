package com.orgzly.android.ui.note.links

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orgzly.R
import com.orgzly.android.App
import com.orgzly.android.data.DataRepository
import com.orgzly.android.ui.util.KeyboardUtils
import com.orgzly.databinding.DialogLinkTargetPickerBinding
import javax.inject.Inject

/** A global, fuzzy-searchable picker for existing note headings. */
class LinkTargetPickerFragment : DialogFragment() {

    private lateinit var binding: DialogLinkTargetPickerBinding

    @Inject
    lateinit var dataRepository: DataRepository

    private lateinit var viewModel: LinkTargetPickerViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            LinkTargetPickerViewModelFactory(dataRepository)
        )[LinkTargetPickerViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme).show()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLinkTargetPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogLinkTargetToolbar.apply {
            title = getString(R.string.insert_link)
            setNavigationOnClickListener { dismiss() }
        }

        val adapter = LinkTargetAdapter(viewModel::select)
        binding.dialogLinkTargetTargets.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        binding.dialogLinkTargetSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
        binding.dialogLinkTargetSearch.setText(viewModel.currentQuery)

        viewModel.targets.observe(viewLifecycleOwner) { targets ->
            adapter.submitList(targets)
            binding.dialogLinkTargetEmpty.visibility =
                if (targets.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.selectedEvent.observeSingle(viewLifecycleOwner) { selection ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    RESULT_NOTE_ID to selection.noteId,
                    RESULT_TITLE to selection.title,
                    RESULT_ID to selection.id
                )
            )
            dismiss()
        }

        viewModel.errorEvent.observeSingle(viewLifecycleOwner) { error ->
            binding.dialogLinkTargetToolbar.subtitle = (error.cause ?: error).localizedMessage
        }

        viewModel.load()
    }

    override fun onResume() {
        super.onResume()

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        requireDialog().window?.setLayout(
            if (height > width) ViewGroup.LayoutParams.MATCH_PARENT else (width * 0.90).toInt(),
            if (height > width) (height * 0.90).toInt() else ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.dialogLinkTargetSearch.post {
            KeyboardUtils.openSoftKeyboard(binding.dialogLinkTargetSearch)
        }
    }

    companion object {
        const val REQUEST_KEY = "note_link_target_picker"
        const val RESULT_NOTE_ID = "note_id"
        const val RESULT_TITLE = "title"
        const val RESULT_ID = "id"

        val FRAGMENT_TAG: String = LinkTargetPickerFragment::class.java.name
    }
}
