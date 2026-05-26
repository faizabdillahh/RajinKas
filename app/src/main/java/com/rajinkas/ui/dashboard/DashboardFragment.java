package com.rajinkas.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.rajinkas.R;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.databinding.FragmentDashboardBinding;
import com.rajinkas.ui.transactions.TransactionAdapter;
import com.rajinkas.ui.transactions.TransactionViewModel;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.util.ClickUtils;
import com.rajinkas.util.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.widget.PopupMenu;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private TransactionViewModel transactionViewModel;
    private SessionManager sessionManager;
    private TransactionAdapter adapter;
    
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new TransactionAdapter();
        binding.rvRecentTransactions.setAdapter(adapter);
        
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionEntity transaction) {
                // View detail
            }

            @Override
            public void onOptionsClick(TransactionEntity transaction, View view) {
                showTransactionOptions(transaction, view);
            }
        });

        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), balance -> {
            binding.tvTotalBalance.setText(formatRupiah(balance));
        });

        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            binding.tvTotalIncome.setText(formatRupiah(income));
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            binding.tvTotalExpense.setText(formatRupiah(expense));
        });

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.submitList(transactions.size() > 5 ? transactions.subList(0, 5) : transactions);
            }
        });

        ClickUtils.applySingleClick(binding.fabAction, v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_add_transaction);
        });

        ClickUtils.applySingleClick(binding.btnBulkAction, v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_bulk_payment);
        });

        setupAnalytics();
    }

    private void setupAnalytics() {
        String currentPeriod = DateUtils.getCurrentPeriodLabel();

        viewModel.getPaidCount(currentPeriod).observe(getViewLifecycleOwner(), paid -> {
            int paidVal = paid != null ? paid : 0;
            binding.tvPaidStatus.setText(getString(R.string.paid_count, paidVal));
            updateProgress();
        });

        viewModel.getUnpaidCount(currentPeriod).observe(getViewLifecycleOwner(), unpaid -> {
            int unpaidVal = unpaid != null ? unpaid : 0;
            binding.tvUnpaidStatus.setText(getString(R.string.unpaid_count, unpaidVal));
            updateProgress();
        });

        viewModel.getTotalArrears().observe(getViewLifecycleOwner(), arrears -> {
            binding.tvTotalArrears.setText(formatRupiah(arrears));
        });

        binding.cardArrears.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_arrears);
        });

        binding.btnViewArrears.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_arrears);
        });
    }

    private void updateProgress() {
        if (binding == null) return;
        
        String paidText = binding.tvPaidStatus.getText().toString();
        String unpaidText = binding.tvUnpaidStatus.getText().toString();
        
        try {
            int paid = Integer.parseInt(paidText.replaceAll("[^0-9]", ""));
            int unpaid = Integer.parseInt(unpaidText.replaceAll("[^0-9]", ""));
            int total = paid + unpaid;
            if (total > 0) {
                int progress = (int) ((paid / (float) total) * 100);
                binding.progressPayment.setProgress(progress, true);
            } else {
                binding.progressPayment.setProgress(0, true);
            }
        } catch (Exception e) {
            binding.progressPayment.setProgress(0, true);
        }
    }

    private void showTransactionOptions(TransactionEntity transaction, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add("Hapus");
        popup.setOnMenuItemClickListener(item -> {
            if ("Hapus".equals(item.getTitle())) {
                confirmDeleteTransaction(transaction);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDeleteTransaction(TransactionEntity transaction) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hapus Transaksi")
                .setMessage("Apakah Anda yakin ingin menghapus transaksi '" + transaction.getDescription() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    transactionViewModel.softDeleteTransaction(transaction, sessionManager.getUserId());
                    Toast.makeText(requireContext(), "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private String formatRupiah(Double amount) {
        return rupiahFormat.format(amount != null ? amount : 0.0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
