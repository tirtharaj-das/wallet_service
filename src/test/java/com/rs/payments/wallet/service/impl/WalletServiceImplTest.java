package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.InsufficientBalanceException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("Should create wallet for existing user")
    void shouldCreateWalletForExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // The service saves the user, which cascades to wallet. 
        // We mock save to return the user.
        when(userRepository.save(user)).thenReturn(user);

        // When
        Wallet result = walletService.createWalletForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(walletService.createWalletForUser(userId).getBalance(), BigDecimal.ZERO);
        
        // Verify interactions
        verify(userRepository, times(2)).findById(userId); // Called twice due to second assert
        verify(userRepository, times(2)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deposit amount successfully")
    void shouldDepositAmountSuccessfully() {
        // Given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100"));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BigDecimal depositAmount = new BigDecimal("50");

        // When
        Wallet result = walletService.deposit(walletId, depositAmount);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150"), result.getBalance());

        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception for negative amount")
    void shouldThrowExceptionForNegativeAmount() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("-10");
        assertThrows(IllegalArgumentException.class, () -> walletService.deposit(walletId, amount));
        verify(walletRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> walletService.deposit(walletId, BigDecimal.TEN));
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    @DisplayName("Should withdraw amount successfully")
    void shouldWithdrawAmountSuccessfully() {
        // Given
        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal withdrawAmount = new BigDecimal("40");

        // When
        Wallet result = walletService.withdraw(walletId, withdrawAmount);

        // Then
        assertEquals(new BigDecimal("60"), result.getBalance());

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient balance")
    void shouldThrowExceptionWhenInsufficientBalance() {
        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("50"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        BigDecimal withdrawAmount = new BigDecimal("100");

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.withdraw(walletId, withdrawAmount));
    }

    @Test
    @DisplayName("Should transfer amount successfully")
    void shouldTransferAmountSuccessfully() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(new BigDecimal("100"));

        Wallet toWallet = new Wallet();
        toWallet.setId(toId);
        toWallet.setBalance(new BigDecimal("50"));

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal amount = new BigDecimal("30");

        Wallet result = walletService.transfer(fromId, toId, amount);

        assertEquals(new BigDecimal("70"), fromWallet.getBalance());
        assertEquals(new BigDecimal("80"), toWallet.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient balance during transfer")
    void shouldFailTransferWhenInsufficientBalance() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(new BigDecimal("20"));

        Wallet toWallet = new Wallet();
        toWallet.setId(toId);
        toWallet.setBalance(new BigDecimal("50"));

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(toWallet));

        BigDecimal amount = new BigDecimal("100");

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.transfer(fromId, toId, amount));
    }
}
