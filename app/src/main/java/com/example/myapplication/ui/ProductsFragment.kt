package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.model.Producto
import com.example.myapplication.databinding.DialogAddEditProductBinding
import com.example.myapplication.databinding.FragmentProductsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onItemClick = { producto ->
                showEditDialog(producto)
            },
            onDeleteClick = { producto ->
                showDeleteConfirmation(producto)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProductsFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.productos.observe(viewLifecycleOwner) { productos ->
            adapter.submitList(productos)
            binding.emptyView.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agregar Producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogBinding.editTextName.text.toString()
                val cantidad = dialogBinding.editTextQuantity.text.toString().toIntOrNull() ?: 0
                val precio = dialogBinding.editTextPrice.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isNotBlank()) {
                    viewModel.addProducto(nombre, cantidad, precio)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(producto: Producto) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        dialogBinding.apply {
            editTextName.setText(producto.nombre)
            editTextQuantity.setText(producto.cantidad.toString())
            editTextPrice.setText(producto.precio.toString())
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = dialogBinding.editTextName.text.toString()
                val cantidad = dialogBinding.editTextQuantity.text.toString().toIntOrNull() ?: 0
                val precio = dialogBinding.editTextPrice.text.toString().toDoubleOrNull() ?: 0.0

                if (nombre.isNotBlank()) {
                    viewModel.updateProducto(producto.id, nombre, cantidad, precio)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(producto: Producto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que quieres eliminar ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteProducto(producto.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 