package com.hbd.coredomain.domain.user;

public enum ProviderType {
	KAKAO,
	GOOGLE,
	DEFAUlT;
	
	public static ProviderType of(String input) {
		try {
			return ProviderType.valueOf(input.toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("존재하지 않는 로그인 방식입니다. : " + input);
		}
	}
}