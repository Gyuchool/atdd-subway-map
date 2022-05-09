package wooteco.subway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.repository.*;
import wooteco.subway.service.dto.LineRequest;
import wooteco.subway.service.dto.SectionRequest;
import wooteco.subway.utils.exception.DuplicatedException;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class SectionServiceTest {

    @Autowired
    private DataSource dataSource;

    private SectionService sectionService;
    private SectionRepository sectionRepository;
    private StationRepository stationRepository;
    private LineRepository lineRepository;

    @BeforeEach
    void setUp() {
        sectionRepository = new SectionRepositoryImpl(dataSource);
        lineRepository = new LineRepositoryImpl(dataSource);
        stationRepository = new StationRepositoryImpl(dataSource);
        sectionService = new SectionService(sectionRepository, stationRepository);
    }

    @DisplayName("구간을 저장한다.")
    @Test
    void create() {
        Station upStation = stationRepository.save(new Station("신림역"));
        Station downStation = stationRepository.save(new Station("신도림역"));
        LineRequest lineRequest = new LineRequest(
                "분당선",
                "bg-red-600",
                upStation.getId(),
                downStation.getId(),
                5
        );

        Line line = new Line(lineRequest.getName(), lineRequest.getColor());
        Line savedLine = lineRepository.save(line);
        SectionRequest sectionRequest = SectionRequest.from(lineRequest);
        Section section = sectionService.create(savedLine.getId(), sectionRequest);

        assertAll(
                () -> assertThat(section.getId()).isNotNull(),
                () -> assertThat(section.getDistance()).isEqualTo(5),
                () -> assertThat(section.getUpStation().getName()).isEqualTo("신림역")
        );
    }

    @DisplayName("이미 존재하는 구간을 추가할 때 예외가 발생한다.")
    @Test
    void createFailureWhenDuplicate() {
        Station upStation = stationRepository.save(new Station("신림역"));
        Station downStation = stationRepository.save(new Station("신도림역"));
        LineRequest lineRequest = new LineRequest(
                "분당선",
                "bg-red-600",
                upStation.getId(),
                downStation.getId(),
                5
        );

        Line line = new Line(lineRequest.getName(), lineRequest.getColor());
        Line savedLine = lineRepository.save(line);
        SectionRequest sectionRequest = new SectionRequest(upStation.getId(), downStation.getId(), 5);

        sectionService.create(savedLine.getId(), sectionRequest);
        assertThatThrownBy(
                () -> sectionService.create(savedLine.getId(), sectionRequest)
        ).isInstanceOf(DuplicatedException.class);

    }
}
