package com.example.InventarioApp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.InventarioApp.data.model.Producto
import com.example.InventarioApp.databinding.DialogAddEditProductBinding
import com.example.InventarioApp.databinding.FragmentProductsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var autoRefreshJob: Job? = null

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
        startAutoRefresh()
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
                val stock = dialogBinding.editTextStock.text.toString().toIntOrNull() ?: 0

                if (nombre.isNotBlank()) {
                    viewModel.addProducto(nombre, cantidad, precio, stock)
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
            editTextStock.setText(producto.stock.toString())
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = dialogBinding.editTextName.text.toString()
                val cantidad = dialogBinding.editTextQuantity.text.toString().toIntOrNull() ?: 0
                val precio = dialogBinding.editTextPrice.text.toString().toDoubleOrNull() ?: 0.0
                val stock = dialogBinding.editTextStock.text.toString().toIntOrNull() ?: 0

                if (nombre.isNotBlank()) {
                    viewModel.updateProducto(producto.id, nombre, cantidad, precio, stock)
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

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                viewModel.loadProductos(showLoading = false)
                delay(2000)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoRefreshJob?.cancel()
        _binding = null
    }
} 