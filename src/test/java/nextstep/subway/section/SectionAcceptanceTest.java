package nextstep.subway.section;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.section.dto.SectionResponse;
import nextstep.subway.station.dto.StationRequest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static java.lang.String.format;
import static nextstep.subway.line.LineAcceptanceTest.라인_생성_및_체크;
import static nextstep.subway.line.LineAcceptanceTest.분당_라인;
import static nextstep.subway.station.StationAcceptanceTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("구간 테스트")
class SectionAcceptanceTest extends AcceptanceTest {
    private static Long 강남역_ID = 1L;
    private static Long 역삼역_ID = 2L;
    private static Long 수진역_ID = 3L;
    private static Long 모란역_ID = 4L;
    private static Long 야탑역_ID = 5L;

    private static Long 분당_라인_ID = 1L;

    // 강남역 <-> 역삼역 <-> 수진역
    private static SectionRequest 역삼역_수진역_길이_15 = new SectionRequest(역삼역_ID, 수진역_ID, 15L);
    private static SectionRequest 수진역_강남역_길이_15 = new SectionRequest(수진역_ID, 강남역_ID, 15L);
    private static SectionRequest 강남역_수진역_길이_15 = new SectionRequest(강남역_ID, 수진역_ID, 15L);
    private static SectionRequest 강남역_수진역_길이_10 = new SectionRequest(강남역_ID, 수진역_ID, 15L);
    
    private static SectionRequest 강남역_역삼역_길이_1 = new SectionRequest(강남역_ID, 역삼역_ID, 1L);
    private static SectionRequest 역삼역_수진역_길이_1 = new SectionRequest(역삼역_ID, 수진역_ID, 1L);
    private static SectionRequest 강남역_수진역_길이_1 = new SectionRequest(강남역_ID, 수진역_ID, 1L);
    private static SectionRequest 역삼역_강남역_길이_1 = new SectionRequest(역삼역_ID, 강남역_ID, 1L);
    private static SectionRequest 수진역_역삼역_길이_1 = new SectionRequest(수진역_ID, 역삼역_ID, 1L);
    private static SectionRequest 수진역_강남역_길이_1 = new SectionRequest(수진역_ID, 강남역_ID, 1L);
    private static SectionRequest 수진역_야탑역_길이_22 = new SectionRequest(수진역_ID, 야탑역_ID, 22L);
    private static SectionRequest 야탑역_수진역_길이_22 = new SectionRequest(야탑역_ID, 수진역_ID, 22L);

    private static SectionRequest 역삼역_모란역_길이_1 = new SectionRequest(역삼역_ID, 모란역_ID, 1L);
    private static SectionRequest 수진역_모란역_길이_1 = new SectionRequest(수진역_ID, 모란역_ID, 1L);
    private static SectionRequest 모란역_강남역_길이_1 = new SectionRequest(모란역_ID, 강남역_ID, 1L);

    @TestFactory
    @DisplayName("신규 구간을 추가한다 강남역 <-> 역삼역 <-> 수진역 => 강남역 <-> 역삼역 <-> 모란역 <-> 수진역 => 강남역 <-> 역삼역 <-> 모란역 <-> 수진역 <-> 야탑역")
    Stream<DynamicTest> 신규_구간을_추가한다_하행() {
        return Stream.of(
                dynamicTest("강남역을 추가한다", 지하철역_생성_요청_및_체크(강남역, 강남역_ID)),
                dynamicTest("역삼역을 추가한다", 지하철역_생성_요청_및_체크(역삼역, 역삼역_ID)),
                dynamicTest("수진역을 추가한다", 지하철역_생성_요청_및_체크(수진역, 수진역_ID)),
                dynamicTest("모란역을 추가한다", 지하철역_생성_요청_및_체크(모란역, 모란역_ID)),
                dynamicTest("야탑역을 추가한다", 지하철역_생성_요청_및_체크(야탑역, 야탑역_ID)),
                dynamicTest("(상)강남역과 (하)역삼역의 노선을 만든다",
                        라인_생성_및_체크(분당_라인, 분당_라인_ID, new StationRequest[]{강남역, 역삼역})
                ),
                dynamicTest("(상)역삼역과 (하)수진역을 연결한다", 구간_생성_및_체크(역삼역_수진역_길이_15, 분당_라인_ID,2L)),
                dynamicTest("(상)역삼역과 (하)모란역을 연결한다", 구간_생성_및_체크(역삼역_모란역_길이_1, 분당_라인_ID,3L)),
                dynamicTest("(상)수진역과 (하)야탑역을 연결한다", 구간_생성_및_체크(수진역_야탑역_길이_22, 분당_라인_ID,4L)),
                dynamicTest("분당라인의 전체 연결을 확인한다", 전체_연결_확인(
                        분당_라인_ID,
                        new ExpectSectionResponse(강남역_ID, 역삼역_ID, 분당_라인.getDistance()),
                        new ExpectSectionResponse(역삼역_ID, 모란역_ID, 역삼역_모란역_길이_1.getDistance()),
                        new ExpectSectionResponse(모란역_ID, 수진역_ID, 역삼역_수진역_길이_15.getDistance() - 역삼역_모란역_길이_1.getDistance()),
                        new ExpectSectionResponse(수진역_ID, 야탑역_ID, 수진역_야탑역_길이_22.getDistance())
                ))
        );
    }

    @TestFactory
    @DisplayName("신규 구간을 추가한다 수진역 <-> 강남역 <-> 역삼역 => 수진역 <-> 모란역 <-> 강남역 <-> 역삼역 => 야탑역 <-> 수진역 <-> 모란역 <-> 강남역 <-> 역삼역")
    Stream<DynamicTest> 신규_구간을_추가한다_상행() {
        return Stream.of(
                dynamicTest("강남역을 추가한다", 지하철역_생성_요청_및_체크(강남역, 강남역_ID)),
                dynamicTest("역삼역을 추가한다", 지하철역_생성_요청_및_체크(역삼역, 역삼역_ID)),
                dynamicTest("수진역을 추가한다", 지하철역_생성_요청_및_체크(수진역, 수진역_ID)),
                dynamicTest("모란역을 추가한다", 지하철역_생성_요청_및_체크(모란역, 모란역_ID)),
                dynamicTest("모란역을 추가한다", 지하철역_생성_요청_및_체크(야탑역, 야탑역_ID)),
                dynamicTest("(상)강남역과 (하)역삼역의 노선을 만든다",
                        라인_생성_및_체크(분당_라인, 분당_라인_ID, new StationRequest[]{강남역, 역삼역})
                ),
                dynamicTest("(상)수진역과 (하)강남역을 연결한다", 구간_생성_및_체크(수진역_강남역_길이_15, 분당_라인_ID,2L)),
                dynamicTest("(상)모란역과 (하)강남역을 연결한다", 구간_생성_및_체크(모란역_강남역_길이_1, 분당_라인_ID,3L)),
                dynamicTest("(상)야탑역과 (하)수진역을 연결한다", 구간_생성_및_체크(야탑역_수진역_길이_22, 분당_라인_ID,4L)),
                dynamicTest("분당라인의 전체 연결을 확인한다", 전체_연결_확인(
                        분당_라인_ID,
                        new ExpectSectionResponse(야탑역_ID, 수진역_ID, 야탑역_수진역_길이_22.getDistance()),
                        new ExpectSectionResponse(수진역_ID, 모란역_ID, 수진역_강남역_길이_15.getDistance() - 모란역_강남역_길이_1.getDistance()),
                        new ExpectSectionResponse(모란역_ID, 강남역_ID, 모란역_강남역_길이_1.getDistance()),
                        new ExpectSectionResponse(강남역_ID, 역삼역_ID, 분당_라인.getDistance())
                ))
        );
    }

    @TestFactory
    @DisplayName("신규 구간 사이에 Section이 존재하며 안된다")
    Stream<DynamicTest> 신규_구간_사이에_Section이_존재하면_안된다() {
        return Stream.of(
                dynamicTest("강남역을 추가한다", 지하철역_생성_요청_및_체크(강남역, 강남역_ID)),
                dynamicTest("역삼역을 추가한다", 지하철역_생성_요청_및_체크(역삼역, 역삼역_ID)),
                dynamicTest("수진역을 추가한다", 지하철역_생성_요청_및_체크(수진역, 수진역_ID)),
                dynamicTest("(상)강남역과 (하)역삼역의 노선을 만든다",
                        라인_생성_및_체크(분당_라인, 분당_라인_ID, new StationRequest[]{강남역, 역삼역})
                ),
                dynamicTest("(상)강남역 (하)수진역을 연결한다", 구간_생성_및_실패_체크(강남역_수진역_길이_15, 분당_라인_ID)),
                dynamicTest("(상)강남역 (하)수진역을 연결한다", 구간_생성_및_실패_체크(강남역_수진역_길이_10, 분당_라인_ID))
        );
    }

    private Executable 구간_생성_및_실패_체크(SectionRequest sectionRequest, Long lineId) {
        return () -> {
            ExtractableResponse<Response> response = 구간_생성_요청(sectionRequest, lineId);

            생성_실패_검증(response);
        };
    }

    @TestFactory
    @DisplayName("이미 연결된 노선은 추가할 수 없다")
    Stream<DynamicTest> 이미_연결된_노선은_추가할_수_없다() {
        return Stream.of(
                dynamicTest("강남역을 추가한다", 지하철역_생성_요청_및_체크(강남역, 강남역_ID)),
                dynamicTest("역삼역을 추가한다", 지하철역_생성_요청_및_체크(역삼역, 역삼역_ID)),
                dynamicTest("수진역을 추가한다", 지하철역_생성_요청_및_체크(수진역, 수진역_ID)),
                dynamicTest("(상)강남역과 (하)역삼역의 노선을 만든다",
                        라인_생성_및_체크(분당_라인, 분당_라인_ID, new StationRequest[]{강남역, 역삼역})
                ),
                dynamicTest("(상)역삼역과 (하)수진역을 연결한다", 구간_생성_및_체크(역삼역_수진역_길이_15, 분당_라인_ID,2L)),
                dynamicTest("이미 연결된 (상)강남역 (하)역삼역을 연결한다", 구간_생성_및_실패_체크(강남역_역삼역_길이_1, 분당_라인_ID)),
                dynamicTest("이미 연결된 (상)역삼역 (하)수진역을 연결한다", 구간_생성_및_실패_체크(역삼역_수진역_길이_1, 분당_라인_ID)),
                dynamicTest("이미 연결된 (상)강남역 (하)수진역을 연결한다", 구간_생성_및_실패_체크(강남역_수진역_길이_1, 분당_라인_ID)),
                dynamicTest("이미 연결된 (상)역삼역 (하)강남역을 연결한다", 구간_생성_및_실패_체크(역삼역_강남역_길이_1, 분당_라인_ID)),
                dynamicTest("이미 연결된 (상)수진역 (하)역삼역을 연결한다", 구간_생성_및_실패_체크(수진역_역삼역_길이_1, 분당_라인_ID)),
                dynamicTest("이미 연결된 (상)수진역 (하)강남역을 연결한다", 구간_생성_및_실패_체크(수진역_강남역_길이_1, 분당_라인_ID))
        );
    }

    @TestFactory
    @DisplayName("상행역 하행역 둘중 하나라도 노선에 포함이 안되어있으면 안된다")
    Stream<DynamicTest> 상행역_하행역_둘중_하나라도_노선에_포함이_안되어있으면_안된다() {
        return Stream.of(
                dynamicTest("강남역을 추가한다", 지하철역_생성_요청_및_체크(강남역, 강남역_ID)),
                dynamicTest("역삼역을 추가한다", 지하철역_생성_요청_및_체크(역삼역, 역삼역_ID)),
                dynamicTest("수진역을 추가한다", 지하철역_생성_요청_및_체크(수진역, 수진역_ID)),
                dynamicTest("모란역을 추가한다", 지하철역_생성_요청_및_체크(모란역, 모란역_ID)),
                dynamicTest("(상)강남역과 (하)역삼역의 노선을 만든다",
                        라인_생성_및_체크(분당_라인, 분당_라인_ID, new StationRequest[]{강남역, 역삼역})
                ),
                dynamicTest("수진역과 모란역을 분당라인에 연결한다", 구간_생성_및_실패_체크(수진역_모란역_길이_1, 분당_라인_ID))
        );
    }

    private void 생성_실패_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private Executable 전체_연결_확인(Long lineId, ExpectSectionResponse ...expectSectionResponses) {
        return () -> {
            ExtractableResponse<Response> response = RestAssured.given()
                    .log().all()
                    .when()
                    .log().all()
                    .get(format("/%d/sections", lineId))
                    .then()
                    .log().all()
                    .extract();

            정상_응답_헤더_검증(response);

            SectionResponse[] sectionResponses = response.as(SectionResponse[].class);

            for (int i = 0; i < expectSectionResponses.length; i++) {
                SectionResponse sectionResponse = sectionResponses[i];
                ExpectSectionResponse expectSectionResponse = expectSectionResponses[i];

                assertThat(sectionResponse.getUpStationId())
                        .isEqualTo(expectSectionResponse.getUpStationId());
                assertThat(sectionResponse.getDownStationId())
                        .isEqualTo(expectSectionResponse.getDownStationId());
                assertThat(sectionResponse.getDistance())
                        .isEqualTo(expectSectionResponse.getDistance());
            }
        };
    }

    private Executable 구간_생성_및_체크(SectionRequest sectionRequest, Long lineId, Long expectId) {
        return () -> {
            ExtractableResponse<Response> response = 구간_생성_요청(sectionRequest, lineId);

            구간_생성_헤더_검증(response);

            SectionResponse sectionResponse = response.as(SectionResponse.class);

            구간_생성_본문_검증(sectionResponse, expectId, sectionRequest);
        };
    }

    private ExtractableResponse<Response> 구간_생성_요청(SectionRequest sectionRequest, Long lineId) {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(sectionRequest)
                .when()
                .log().all()
                .post(format("/%d/sections", lineId))
                .then().extract();
        return response;
    }

    private void 구간_생성_본문_검증(SectionResponse sectionResponse, Long expectId, SectionRequest sectionRequest) {
        assertThat(sectionResponse.getId())
                .isEqualTo(expectId);
        assertThat(sectionResponse.getUpStationId())
                .isEqualTo(sectionRequest.getUpStationId());
        assertThat(sectionResponse.getDownStationId())
                .isEqualTo(sectionRequest.getDownStationId());
        assertThat(sectionResponse.getDistance())
                .isEqualTo(sectionRequest.getDistance());
    }

    private void 구간_생성_헤더_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode())
                .isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header(HttpHeaders.CONTENT_TYPE))
                .isIn(ContentType.JSON.getContentTypeStrings());
    }

    private void 정상_응답_헤더_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.header(HttpHeaders.CONTENT_TYPE)).isIn(ContentType.JSON.getContentTypeStrings());
    }

    private class ExpectSectionResponse {
        private Long upStationId;
        private Long downStationId;
        private Long distance;

        public ExpectSectionResponse(Long upStationId, Long downStationId, Long distance) {
            this.upStationId = upStationId;
            this.downStationId = downStationId;
            this.distance = distance;
        }

        public Long getUpStationId() {
            return upStationId;
        }

        public Long getDownStationId() {
            return downStationId;
        }

        public Long getDistance() {
            return distance;
        }
    }
}