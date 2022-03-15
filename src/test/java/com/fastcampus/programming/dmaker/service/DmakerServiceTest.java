package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.code.StatusCode;
import com.fastcampus.programming.dmaker.dto.CreateDeveloper;
import com.fastcampus.programming.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.exception.DMakerErrorCode;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import com.fastcampus.programming.dmaker.type.DeveloperSkillType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fastcampus.programming.dmaker.constant.DMakerConstant.MAX_JUINOR_EXPERIENCE_YEARS;
import static com.fastcampus.programming.dmaker.constant.DMakerConstant.MIN_SEINOR_EXPERIENCE_YEARS;
import static com.fastcampus.programming.dmaker.exception.DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED;
import static com.fastcampus.programming.dmaker.type.DeveloperLevel.*;
import static com.fastcampus.programming.dmaker.type.DeveloperSkillType.FRONT_END;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
class DmakerServiceTest {
    @Mock
    private DeveloperRepository developerRepository;

    @InjectMocks
    private DmakerService dmakerService;

    private final Developer defaultDeveloper = Developer.builder()
            .developerLevel(SENIOR)
            .developerSkillType(FRONT_END)
            .experienceYears(12)
            .statusCode(StatusCode.EMPLOYED)
            .name("name")
            .age(12)
            .build();

    private CreateDeveloper.Request getCreateRequest(
            DeveloperLevel developerLevel,
            DeveloperSkillType developerSkillType,
            Integer experienceYears
    ) {
            return CreateDeveloper.Request.builder()
            .developerLevel(developerLevel)
            .developerSkillType(developerSkillType)
            .experienceYears(experienceYears)
            .memberId("memberId")
            .name("name")
            .age(32)
            .build();
    }

    @Test
    public void testSomething() {
        // given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));

        // when
        DeveloperDetailDto developerdetail = dmakerService.getDeveloperDetail("memberId");

        // then
        assertEquals(SENIOR, developerdetail.getDeveloperLevel());
        assertEquals(FRONT_END, developerdetail.getDeveloperSkillType());
        assertEquals(12, developerdetail.getExperienceYears());
    }

    @Test
    void createDeveloperTest_success() {
        // given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.empty());
        given(developerRepository.save(ArgumentMatchers.any()))
                .willReturn(defaultDeveloper);
        // DB에 저장되는 데이터가 뭔지 확인하고 싶을때
        ArgumentCaptor<Developer> captor = ArgumentCaptor.forClass(Developer.class);

        // when
        dmakerService.createDeveloper(getCreateRequest(SENIOR, FRONT_END, MIN_SEINOR_EXPERIENCE_YEARS));

        // then
        verify(developerRepository, times(1))
                .save(captor.capture());
        Developer savedDeveloper = captor.getValue();
        assertEquals(SENIOR, savedDeveloper.getDeveloperLevel());
        assertEquals(FRONT_END, savedDeveloper.getDeveloperSkillType());
        assertEquals(12, savedDeveloper.getExperienceYears());
    }

    @Test
    void createDeveloperTest_failed_with_duplicated() {
        // given
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));

        // when
        // then
        DMakerException dMakerException =
                assertThrows(DMakerException.class,
                        () -> dmakerService.createDeveloper(getCreateRequest(SENIOR, FRONT_END, MIN_SEINOR_EXPERIENCE_YEARS)));

        assertEquals(DMakerErrorCode.DUPLICATED_MEMBER_ID, dMakerException.getDMakerErrorCode());
    }

    @Test
    void createDeveloperTest_fail_with_unmatched_level() {
        // when
        // then
        DMakerException dMakerException =
                assertThrows(DMakerException.class,
                        () -> dmakerService.createDeveloper(
                                getCreateRequest(SENIOR, FRONT_END,
                                        MIN_SEINOR_EXPERIENCE_YEARS-1)
                        )
                );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());

        dMakerException = assertThrows(DMakerException.class,
                () -> dmakerService.createDeveloper(
                        getCreateRequest(JUNIOR, FRONT_END,
                                MAX_JUINOR_EXPERIENCE_YEARS+1)
                )
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());

        dMakerException = assertThrows(DMakerException.class,
                () -> dmakerService.createDeveloper(
                        getCreateRequest(JUNGNIOR, FRONT_END,
                                MIN_SEINOR_EXPERIENCE_YEARS+1)
                )
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, dMakerException.getDMakerErrorCode());
    }
}