package com.rajinkas.ui.transactions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.appcompat.widget.PopupMenu;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.R;
import com.rajinkas.databinding.FragmentTransactionsBinding;

public class TransactionsFragment extends Fragment {
    private FragmentTransactionsBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new TransactionAdapter();
        binding.rvTransactions.setAdapter(adapter);
        
        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionEntity transaction) {
                // View detail or Edit
            }

            @Override
            public void onOptionsClick(TransactionEntity transaction, View view) {
                showOptionsPopup(transaction, view);
            }
        });

        observeTransactions("");

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                observeTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_add_transaction);
        });
    }

    private void showOptionsPopup(TransactionEntity transaction, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(getString(R.string.delete));
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.delete))) {
                confirmDelete(transaction);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDelete(TransactionEntity transaction) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.delete_transaction_confirm, transaction.getDescription()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.softDeleteTransaction(transaction, sessionManager.getUserId());
                    Toast.makeText(requireContext(), R.string.success_delete, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeTransactions(String query) {
        viewModel.searchTransactions(query).observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            if (transactions == null || transactions.isEmpty()) {
                binding.rvTransactions.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvTransactions.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
