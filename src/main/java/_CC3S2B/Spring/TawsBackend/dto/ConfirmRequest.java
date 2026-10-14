package _CC3S2B.Spring.TawsBackend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
public record ConfirmRequest(@NotNull UUID proposalId) {}
