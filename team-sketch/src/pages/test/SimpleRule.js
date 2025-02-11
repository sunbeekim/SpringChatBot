import React from 'react';

const SimpleRule = ({ 
    rules, 
    isSelectionMode, 
    selectedRules, 
    handleSelectRule, 
    handleSelectAllRules,
    handleEditRule,
    handleDeleteRule 
}) => {
    // 적용되지 않은 규칙만 필터링
    const simpleRules = rules.filter(rule => 
        rule.type === 'simple' && !rule.applied
    );

    return (
        <div className="card mb-4">
            <div className="card-header d-flex justify-content-between align-items-center">
                <h3 className="mb-0">단순 응답 규칙 목록</h3>
                {isSelectionMode && simpleRules.length > 0 && (
                    <div className="form-check">
                        <input
                            type="checkbox"
                            className="form-check-input"
                            checked={simpleRules.every(rule => selectedRules.includes(rule.id))}
                            onChange={() => handleSelectAllRules('simple')}
                            id="selectAllSimple"
                        />
                        <label className="form-check-label" htmlFor="selectAllSimple">
                            전체 선택
                        </label>
                    </div>
                )}
            </div>
            <div className="card-body">
                <div className="table-responsive">
                    <table className="table table-hover">
                        <thead className="table-light">
                            <tr>
                                {isSelectionMode && <th width="50px"></th>}
                                <th>트리거 단어</th>
                                <th>응답 내용</th>
                                <th width="150px">작업</th>
                            </tr>
                        </thead>
                        <tbody>
                            {simpleRules.map((rule) => (
                                <tr key={rule.id}>
                                    {isSelectionMode && (
                                        <td>
                                            <div className="form-check">
                                                <input
                                                    type="checkbox"
                                                    className="form-check-input"
                                                    checked={selectedRules.includes(rule.id)}
                                                    onChange={() => handleSelectRule(rule.id)}
                                                />
                                            </div>
                                        </td>
                                    )}
                                    <td>{rule.triggerWords.join(', ')}</td>
                                    <td>{rule.response}</td>
                                    <td>
                                        <div className="d-flex gap-2">
                                            <button 
                                                onClick={() => handleEditRule(rule)}
                                                className="btn btn-outline-primary btn-sm"
                                            >
                                                수정
                                            </button>
                                            <button 
                                                onClick={() => handleDeleteRule(rule.id)}
                                                className="btn btn-outline-danger btn-sm"
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default SimpleRule;