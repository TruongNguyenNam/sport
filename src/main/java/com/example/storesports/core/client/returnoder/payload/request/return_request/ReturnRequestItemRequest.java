package com.example.storesports.core.client.returnoder.payload.request.return_request;

import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaRequest;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestItemRequest {
    private List<ReturnMediaRequest> mediaRequests;
    private Long orderItemId;
    private Integer quantity;
    private String reason;
    private String note;

}
