package kh.edu.iam.persistence.adapter;

import kh.edu.iam.User;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.query.UserFilterQuery;
import kh.edu.iam.persistence.entity.UserEntity;
import kh.edu.iam.persistence.mapper.UserPersistenceMapper;
import kh.edu.iam.persistence.repository.UserJpaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepository {
    private final UserJpaRepository repository;

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity;
        if (user.getId() == null) {
            entity = new UserEntity();
        } else {
            entity = repository.findById(user.getId()).orElseThrow();
            if (!entity.getUuid().equals(user.getUuid())) {
                throw new IllegalArgumentException("User ID mismatch");
            }
            if (!Objects.equals(entity.getVersion(), user.getAudit().version())) {
                throw new ObjectOptimisticLockingFailureException(UserEntity.class, user.getId());
            }
        }
        UserPersistenceMapper.apply(user, entity);
        return UserPersistenceMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).filter(e -> !e.isDeleted()).map(UserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<User> lockByUuid(UUID uuid) {
        return repository.findForUpdate(uuid).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String login) {
        return repository
                .findByLogin(login, login.strip().toLowerCase(Locale.ROOT))
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<User> lockByUsernameOrEmail(String login) {
        return repository
                .findByLoginForUpdate(login, login.strip().toLowerCase(Locale.ROOT))
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email.strip().toLowerCase(Locale.ROOT));
    }

    @Override
    public PageResult<User> findAll(UserFilterQuery q) {
        var sort = Sort.by(q.descending() ? Sort.Direction.DESC : Sort.Direction.ASC, q.sortBy()).and(Sort.by("id"));
        var page = repository.search(q.search(), q.enabled(), q.locked(), PageRequest.of(q.page(), q.size(), sort));
        return new PageResult<>(
                page.getContent().stream().map(UserPersistenceMapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
