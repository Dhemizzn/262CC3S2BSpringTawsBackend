package _CC3S2B.Spring.TawsBackend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConfirmRequest(
        @NotNull UUID proposalId
) {}