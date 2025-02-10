import React from 'react';

const ConditionRule = ({ 
    rules, 
    isSelectionMode, 
    selectedRules, 
    handleSelectRule, 
    handleSelectAllRules,
    handleEditRule,
    handleDeleteRule 
}) => {
    // 적용되지 않은 규칙만 필터링
    const conditionalRules = rules.filter(rule => 
        rule.type === 'conditional' && !rule.applied
    );

    return (
        <div className="rules-section">
            <h3>조건부 응답 규칙 목록</h3>
            {isSelectionMode && conditionalRules.length > 0 && (
                <div className="select-all-container">
                    <label className="select-all-label">
                        <input
                            type="checkbox"
                            className="select-all-checkbox"
                            checked={conditionalRules.every(rule => selectedRules.includes(rule.id))}
                            onChange={() => handleSelectAllRules('conditional')}
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
                            <th>기본 응답</th>
                            <th>조건부 응답</th>
                            <th width="150px">작업</th>
                        </tr>
                    </thead>
                    <tbody>
                        {conditionalRules.map((rule) => (
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
                                    <div className="conditions-list">
                                        {rule.conditions.map((condition, index) => (
                                            <div key={index} className="condition-item">
                                                <strong>{condition.conditionText}:</strong> {condition.response}
                                            </div>
                                        ))}
                                    </div>
                                </td>
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

export default ConditionRule;
