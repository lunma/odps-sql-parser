package com.sea.odps.service.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 校验结果。 */
public class ValidationResult {

    private final boolean valid;
    private final List<ValidationError> errors;

    public ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors =
                errors != null
                        ? Collections.unmodifiableList(new ArrayList<>(errors))
                        : Collections.emptyList();
    }

    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        if (valid) {
            return "✓ 校验通过";
        }
        StringBuilder sb = new StringBuilder("✗ 校验失败:\n");
        for (ValidationError error : errors) {
            sb.append("  - ").append(error).append("\n");
        }
        return sb.toString();
    }
}
