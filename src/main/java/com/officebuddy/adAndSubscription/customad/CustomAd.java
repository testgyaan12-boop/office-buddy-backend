package com.officebuddy.adAndSubscription.customad;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "custom_ads")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAd extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "product_img_link", length = 1024)
    private String productImgLink;

    @Column(name = "product_open_link", length = 1024)
    private String productOpenLink;
}
