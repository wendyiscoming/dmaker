package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.dto.CreateDeveloper;
import com.fastcampus.programming.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.programming.dmaker.dto.DeveloperDto;
import com.fastcampus.programming.dmaker.dto.EditDeveloper;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.entity.RetiredDeveloper;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.repository.RetiredDeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.fastcampus.programming.dmaker.constant.DMakerConstant.MAX_JUINOR_EXPERIENCE_YEARS;
import static com.fastcampus.programming.dmaker.constant.DMakerConstant.MIN_SEINOR_EXPERIENCE_YEARS;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.*;

@Service
@RequiredArgsConstructor    // autowired -> 생성자 방식에서 어노테이션으로
//@Transactional  // entitymanager로 transaction할 필요없다. AOP기반으로 동작함 TransactionInterceptor가 동작함
public class DmakerService {
    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloperRepository;
//    private final EntityManager em; // db를 추상화

    // transaction - ACID
    // Atomic 원자성
    // Consistency 일관성
    // Isolation 고립성
    // Durability 지속성
    @Transactional
    public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {
        validateCreatedDeveloperRequest(request);
        // business logic start
        // 지역변수 선언하지 않은 응집력 있는 코드
        return CreateDeveloper.Response.fromEntity(
                developerRepository.save(
                        createDeveloperFromRequest(request)
                ));
        // business logic end
    }

    private Developer createDeveloperFromRequest(CreateDeveloper.Request request) {
        return
                Developer.builder()
                        .developerLevel(request.getDeveloperLevel())
                        .developerSkillType(request.getDeveloperSkillType())
                        .experienceYears(request.getExperienceYears())
                        .memberId(request.getMemberId())
                        .name(request.getName())
                        .age(request.getAge())
                        .statusCode(StatusCode.EMPLOYED)
                        .build();
    }

    private void validateCreatedDeveloperRequest(
            @NonNull CreateDeveloper.Request request
    ) {
        // business validation
        request.getDeveloperLevel().validateExperienceYears(request.getExperienceYears());

        developerRepository.findByMemberId(request.getMemberId())
                .ifPresent(developer -> {
                    throw new DMakerException(DUPLICATED_MEMBER_ID);
                });
    }

    private void validateDeveloperLevel(DeveloperLevel developerLevel, Integer experienceYears) {
        developerLevel.validateExperienceYears(experienceYears);
    }

    @Transactional(readOnly = true)
    public List<DeveloperDto> getAllEmployedDevelopers() {
        return developerRepository.findDevelopersByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return DeveloperDetailDto.fromEntity(getDeveloperByMemberId(memberId));
    }

    private Developer getDeveloperByMemberId(String memberId) {
        return developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
    }

    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {
        request.getDeveloperLevel().validateExperienceYears(
                request.getExperienceYears()
        );

        return DeveloperDetailDto.fromEntity(
                getUpdatedDeveloperFromRequest(
                        request, getDeveloperByMemberId(memberId)
                )
        );
    }

    private Developer getUpdatedDeveloperFromRequest(
            EditDeveloper.Request request, Developer developer
    ) {
        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return developer;
    }

    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        // 1. EMPLOYED -> RETIRED
        Developer developer =
                developerRepository.findByMemberId(memberId).orElseThrow(() -> new DMakerException(NO_DEVELOPER));
        developer.setStatusCode(StatusCode.RETIRED);
        // 2. save into RetiredDeveloper
        RetiredDeveloper retiredDeveloper = RetiredDeveloper.builder()
                .memberId(memberId)
                .name(developer.getName())
                .build();
        retiredDeveloperRepository.save(retiredDeveloper);

        return DeveloperDetailDto.fromEntity(developer);
    }
}
