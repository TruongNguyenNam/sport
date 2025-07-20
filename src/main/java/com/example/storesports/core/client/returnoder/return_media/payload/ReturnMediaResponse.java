package com.example.storesports.core.client.returnoder.return_media.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnMediaResponse {
    private Long returnRequestItemId;
    private String url;
    private String fileName;
    private String type;
}
