package com.example.InventarioApp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.InventarioApp.data.AppDatabase
import com.example.InventarioApp.databinding.FragmentDashboardBinding
import com.example.InventarioApp.repository.ProductRepository
import com.example.InventarioApp.viewmodel.ProductViewModel
import com.example.InventarioApp.viewmodel.ProductViewModelFactory

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ProductRepository(database.productDao())
        viewModel = ViewModelProvider(this, ProductViewModelFactory(repository))[ProductViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            binding.totalProductsValue.text = products.size.toString()
            binding.totalValueValue.text = String.format("%.2f", products.sumOf { it.price * it.quantity })
        }

        viewModel.lowStockProducts.observe(viewLifecycleOwner) { products ->
            binding.lowStockValue.text = products.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 