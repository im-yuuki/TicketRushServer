package me.june8th.ticketrushserver.types;

import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.utils.Validator;

import java.util.Optional;

public record UpdateOrganizationInfoPayload(
        Optional<String> description,
        Optional<String> aliasName,
        Optional<String> websiteUrl
) {

    public OrganizationAccount patchOrganization(OrganizationAccount org) {
        Validator validator = new Validator();
        description.ifPresent(description -> {
            validator.validateNotBlank(description);
            org.setDescription(description);
        });
        aliasName.ifPresent(aliasName -> {
            validator.validateName(aliasName);
            org.setAliasName(aliasName);
        });
        websiteUrl.ifPresent(websiteUrl -> {
            validator.validateUri(websiteUrl);
            org.setWebsiteUrl(websiteUrl);
        });
        validator.throwExceptionIfInvalid();
        return org;
    }

}
