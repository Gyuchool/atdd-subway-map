package wooteco.subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.repository.SectionRepository;
import wooteco.subway.domain.repository.StationRepository;
import wooteco.subway.service.dto.SectionRequest;
import wooteco.subway.utils.exception.DuplicatedException;
import wooteco.subway.utils.exception.NotFoundException;

import java.util.List;

@Transactional
@Service
public class SectionService {
    private static final String NOT_FOUND_STATION_MESSAGE = "[ERROR] %d 식별자에 해당하는 역을 찾을수 없습니다.";

    private final SectionRepository sectionRepository;
    private final StationRepository stationRepository;

    public SectionService(SectionRepository sectionRepository, StationRepository stationRepository) {
        this.sectionRepository = sectionRepository;
        this.stationRepository = stationRepository;
    }

    public Section create(Long lineId, SectionRequest sectionRequest) {
        //TODO: validateExist >> 노선에 해당 section이 있다면 추가 불가능
        List<Section> sections = sectionRepository.findAllByLineId(lineId);
        validateDuplicate(sectionRequest, sections);

        Station upStation = stationRepository.findById(sectionRequest.getUpStationId())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_STATION_MESSAGE, sectionRequest.getUpStationId())));
        Station downStation = stationRepository.findById(sectionRequest.getDownStationId())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_STATION_MESSAGE, sectionRequest.getDownStationId())));
        Section section = Section.create(lineId, upStation, downStation, sectionRequest.getDistance());
        return sectionRepository.save(section);
    }

    private void validateDuplicate(SectionRequest sectionRequest, List<Section> sections) {
        sections.stream()
                .filter(section -> section.getDownStation().getId().equals(sectionRequest.getDownStationId()) &&
                        section.getUpStation().getId().equals(sectionRequest.getUpStationId()))
                .forEach(section -> {
                    throw new DuplicatedException("[ERROR] 이미 노선에 존재하는 구간입니다.");
                });
    }
}
