package br.com.diegocordeiro.dscproject.enums;

import lombok.Getter;

@Getter
public enum TipoRedeSocial {
    LINKEDIN("LinkedIn", "ph ph-linkedin-logo"),
    GITHUB("GitHub", "ph ph-github-logo"),
    FACEBOOK("Facebook", "ph ph-facebook-logo"),
    INSTAGRAM("Instagram", "ph ph-instagram-logo"),
    TWITTER_X("X (Twitter)", "ph ph-x-logo"),
    YOUTUBE("YouTube", "ph ph-youtube-logo"),
    OUTRO("Outro", "ph ph-link");

    private final String descricao;
    private final String icone;

    TipoRedeSocial(String descricao, String icone) {
        this.descricao = descricao;
        this.icone = icone;
    }
}
