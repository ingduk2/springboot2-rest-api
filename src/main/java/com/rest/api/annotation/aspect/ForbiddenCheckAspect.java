package com.rest.api.annotation.aspect;

import com.rest.api.advice.exception.CForbiddenWordException;
import com.rest.api.annotation.ForbiddenWordCheck;
import io.micrometer.core.instrument.util.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
public class ForbiddenCheckAspect {

    //어노테이션 설정된 메서드가 실행되기 전에 금칙어 체크 적용
    @Before(value = "@annotation(forbiddenWordCheck)")
    public void forbiddenWordCheck(JoinPoint pjp, ForbiddenWordCheck forbiddenWordCheck) throws Throwable {
        //금칙어를 체크할 파라미터가 객체인지 (객체.필드명) 일반 String 인지에 따라 구분하여 처리.
        String[] param = forbiddenWordCheck.param().split("\\.");
        String paramName;
        String fieldName = "";
        if (param.length == 2) {
            paramName = param[0];
            fieldName = param[1];
        } else {
            paramName = forbiddenWordCheck.param();
        }

        //파라미터 이름으로 메서드의 몇번째 파라미터인지 구한다.
        Integer parameterIdx = getParameterIdx(pjp, paramName);
        if (parameterIdx == -1) {
            throw new IllegalArgumentException();
        }

        String checkWord;
        // 금칙어 체크할 문장을 객체내의 필드값에서 알아내야 할 경우(리플렉션 이용)
        if (StringUtils.isNotEmpty(fieldName)) {
            Class<?> clazz = forbiddenWordCheck.checkClazz();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            checkWord = (String) field.get(pjp.getArgs()[parameterIdx]);

        }
        //금칙어가 String 으로 넘어올 경우
        else {
            checkWord = (String) pjp.getArgs()[parameterIdx];
        }

        checkForbiddenWord(checkWord);
    }

    //메서드의 파라미터 이름으로 몇번쨰인지 idx 구함
    private Integer getParameterIdx(JoinPoint pjp, String paramName) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (paramName.equals(parameterName)) {
                return i;
            }
        }
        return -1;
    }

    public void checkForbiddenWord(String word) {
        List<String> forbiddenWords = Arrays.asList("개새끼", "썅년", "시발", "씨발");
        Optional<String> forbiddenWord = forbiddenWords.stream().filter(word::contains).findFirst();
        if (forbiddenWord.isPresent()) {
            throw new CForbiddenWordException(forbiddenWord.get());
        }
    }
}
