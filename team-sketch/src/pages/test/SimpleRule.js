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
        <div className="rules-section">
            <div className="section-header">
                <h3>단순 응답 규칙 목록</h3>
            </div>
            {isSelectionMode && simpleRules.length > 0 && (
                <div className="select-all-container">
                    <label className="select-all-label">
                        <input
                            type="checkbox"
                            className="select-all-checkbox"
                            checked={simpleRules.every(rule => selectedRules.includes(rule.id))}
                            onChange={() => handleSelectAllRules('simple')}
                        />
                        전체 선택
                    </label>
                </div>
            )}
            <div className="rules-table">
                <table>
                    <thead>
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
                                        <input
                                            type="checkbox"
                                            className="rule-checkbox"
                                            checked={selectedRules.includes(rule.id)}
                                            onChange={() => handleSelectRule(rule.id)}
                                        />
                                    </td>
                                )}
                                <td>{rule.triggerWords.join(', ')}</td>
                                <td>{rule.response}</td>
                                <td>
                                    <div className="button-group">
                                        <button 
                                            onClick={() => handleEditRule(rule)}
                                            className="button-small edit"
                                        >
                                            수정
                                        </button>
                                        <button 
                                            onClick={() => handleDeleteRule(rule.id)}
                                            className="button-small delete"
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
    );
};

export default SimpleRule;