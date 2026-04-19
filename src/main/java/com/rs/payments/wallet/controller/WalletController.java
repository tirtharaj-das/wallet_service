package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new wallet for a user",
            description = "Creates a new wallet for the specified user ID with a zero balance.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Wallet created successfully",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWalletForUser(request.getUserId());
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Deposit amount into wallet",
            description = "Deposits the specified amount into the given wallet ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Deposit successful",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid amount"
                    )
            }
    )
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Wallet> deposit(
            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID walletId,

            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount
    ) {
        Wallet wallet = walletService.deposit(walletId, amount);
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Withdraw amount from wallet",
            description = "Withdraws the specified amount from the given wallet ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Withdrawal successful",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid amount or insufficient balance"
                    )
            }
    )
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID walletId,

            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount
    ) {
        Wallet wallet = walletService.withdraw(walletId, amount);
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Transfer amount between wallets",
            description = "Transfers amount from one wallet to another.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = Wallet.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid amount or insufficient balance"
                    )
            }
    )
    @PostMapping("/transfer")
    public ResponseEntity<Wallet> transfer(
            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.RequestParam java.util.UUID fromWalletId,

            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.RequestParam java.util.UUID toWalletId,

            @jakarta.validation.constraints.NotNull
            @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount
    ) {
        Wallet wallet = walletService.transfer(fromWalletId, toWalletId, amount);
        return ResponseEntity.ok(wallet);
    }
}