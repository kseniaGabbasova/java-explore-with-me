package statsClient;

import dto.HitEndpointDto;
import dto.StatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate template;
    private final String appName;

    public StatsClient(@Value("${stat-server.url}") String url,
                      @Value("${application.name}") String appName,
                      RestTemplateBuilder template) {
        this.appName = appName;
        this.template = template
                .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                .build();
    }

    public void addHit(HttpServletRequest request) {
        HitEndpointDto endpointHitDto = new HitEndpointDto(
                null,
                "explore-main",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        template.postForEntity("/hit",
                new HttpEntity<>(endpointHitDto),
                HitEndpointDto.class);
    }

    public ResponseEntity<List<StatDto>> getStat(String start,
                                                 String end,
                                                 List<String> uris,
                                                 boolean unique) {
        return template.exchange("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                getHttpEntity(null),
                new ParameterizedTypeReference<>() {
                },
                start, end, uris, unique);
    }

    private <T> HttpEntity<T> getHttpEntity(T dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return dto == null ? new HttpEntity<>(headers) : new HttpEntity<>(dto, headers);
    }
}
