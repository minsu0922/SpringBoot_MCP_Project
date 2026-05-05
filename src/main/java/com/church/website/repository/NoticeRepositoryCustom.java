package com.church.website.repository;

import com.church.website.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeRepositoryCustom {

    List<Notice> searchNotices(String keyword, String popupStatus);

    Page<Notice> searchNotices(String keyword, String popupStatus, Pageable pageable);
}
