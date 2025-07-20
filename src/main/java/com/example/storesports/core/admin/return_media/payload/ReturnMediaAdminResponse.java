package com.example.storesports.core.admin.return_media.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnMediaAdminResponse {
    private Long returnRequestItem;

    private String url;

    private String type;
}
