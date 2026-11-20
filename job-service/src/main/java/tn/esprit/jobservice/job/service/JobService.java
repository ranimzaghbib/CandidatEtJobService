package tn.esprit.jobservice.job.service;


import tn.esprit.jobservice.common.exception.ConflictException;
import tn.esprit.jobservice.common.exception.ResourceNotFoundException;
import tn.esprit.jobservice.job.dto.JobRequest;
import tn.esprit.jobservice.job.dto.JobResponse;
import tn.esprit.jobservice.job.entity.Job;
import tn.esprit.jobservice.job.mapper.JobMapper;
import tn.esprit.jobservice.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final CategoryService categoryService;

    public JobService(JobRepository jobRepository, CategoryService categoryService) {
        this.jobRepository = jobRepository;
        this.categoryService = categoryService;
    }

    /** Lists jobs, optionally filtered by availability and/or category. */
    @Transactional(readOnly = true)
    public List<JobResponse> search(Boolean available, Long categoryId) {
        if (categoryId != null) {
            categoryService.getCategory(categoryId); // 404 if the category does not exist
        }
        return jobRepository.search(available, categoryId).stream().map(JobMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        return JobMapper.toResponse(getJob(id));
    }

    public JobResponse create(JobRequest request) {
        Job job = new Job();
        JobMapper.updateEntity(job, request, categoryService.getCategory(request.categoryId()));
        return JobMapper.toResponse(jobRepository.save(job));
    }

    public JobResponse update(Long id, JobRequest request) {
        Job job = getJob(id);
        JobMapper.updateEntity(job, request, categoryService.getCategory(request.categoryId()));
        return JobMapper.toResponse(job);
    }

    /** Opens or closes a job without sending the whole object. */
    public JobResponse setAvailability(Long id, boolean available) {
        Job job = getJob(id);
        job.setAvailable(available);
        return JobMapper.toResponse(job);
    }

    public void delete(Long id) {
        Job job = getJob(id);
        jobRepository.delete(job);
    }

    /** Also used by the candidature module (in-process call inside the monolith). */
    public Job getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }
}
