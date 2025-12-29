package com.strategytracker.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.strategytracker.databinding.DialogAddStrategyBinding

class AddStrategyDialog(
    private val onStrategyAdded: (String) -> Unit
) : DialogFragment() {
    
    private var _binding: DialogAddStrategyBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddStrategyBinding.inflate(LayoutInflater.from(context))
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Strategy")
            .setView(binding.root)
            .setPositiveButton("Create") { _, _ ->
                val name = binding.etStrategyName.text.toString().trim()
                if (name.isNotEmpty()) {
                    onStrategyAdded(name)
                } else {
                    Toast.makeText(context, "Please enter a strategy name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
