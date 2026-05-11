package me.june8th.ticketrushserver.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public final class OrganizationProfileView extends ProfileView {

    private String avatarUrl;
    private String bannerUrl;
    private String aliasName;
    private String description;
    private String websiteUrl;

}
