package wooteco.subway.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import wooteco.subway.domain.Station;
import wooteco.subway.service.StationService;
import wooteco.subway.service.dto.StationRequest;
import wooteco.subway.service.dto.StationResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("지하철 역 API 문서화")
@AutoConfigureRestDocs
@WebMvcTest(StationController.class)
@Import(RestDocsConfig.class)
class StationControllerTest {

    @MockBean
    private StationService stationService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("역 생성 문서화")
    void createStation() throws Exception {
        Station 신설역 = new Station(1L, "신설역");
        StationResponse stationResponse = new StationResponse(신설역);
        StationRequest request = new StationRequest("신설역");

        given(stationService.save(any())).willReturn(stationResponse);
        String content = objectMapper.writeValueAsString(request);
        System.out.println(content);
        ResultActions results = mvc.perform(post("/stations")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        results.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("station-create",
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("식별자"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("역 이름").optional()
                        )
                ));

    }
}
