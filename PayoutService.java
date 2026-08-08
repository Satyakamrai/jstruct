package com.company.payouts.service;

import com.company.payouts.repository.UserRepository;
import com.company.payouts.repository.TransactionRepository;
import com.company.payouts.model.PayoutRequest;
import com.company.payouts.model.User;
import com.company.payouts.model.Transaction;
import com.company.payouts.integration.BankApiClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayoutService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Shared state
    private int totalProcessed = 0; 

    public void process(List<PayoutRequest> requests) throws Exception {
        for (PayoutRequest request : requests) {
            
            User user = userRepository.findById(request.getUserId()).orElse(null);
            
            if (user != null && user.getBalance() >= request.getAmount()) {
                
                double fee = request.getAmount() * 0.015; 
                double payoutAmount = request.getAmount() - fee;

                // Deduct balance and save
                user.setBalance(user.getBalance() - request.getAmount());
                userRepository.save(user); 

                Transaction txn = new Transaction(user.getId(), payoutAmount, "PROCESSING");
                transactionRepository.save(txn);

                // Synchronous external network call
                BankApiClient bankClient = new BankApiClient();
                boolean success = bankClient.transfer(user.getBankAccount(), payoutAmount);

                if (success) {
                    txn.setStatus("COMPLETED");
                    totalProcessed++;
                } else {
                    txn.setStatus("FAILED");
                }
                
                transactionRepository.save(txn);
            }
        }
    }
    
    public int getTotalProcessed() {
        return totalProcessed;
    }
}
