import React, { useState } from 'react';

const AppliedRules = ({ rules, onUnapplyRules }) => {
    const [isSelectionMode, setIsSelectionMode] = useState(false);
    const [selectedRules, setSelectedRules] = useState([]);
    const simpleRules = rules.filter(rule => rule.type === 'simple');
    const conditionalRules = rules.filter(rule => rule.type === 'conditional');

    const handleSelectRule = (ruleId) => {
        setSelectedRules(prev => 
            prev.includes(ruleId) 
                ? prev.filter(id => id !== ruleId)
                : [...prev, ruleId]
        );
    };

    const handleSelectAllRules = (type) => {
        const typeRules = rules.filter(rule => rule.type === type);
        const typeRuleIds = typeRules.map(rule => rule.id);
        
        if (typeRules.every(rule => selectedRules.includes(rule.id))) {
            setSelectedRules(prev => prev.filter(id => !typeRuleIds.includes(id)));
        } else {
            setSelectedRules(prev => {
                const filteredPrev = prev.filter(id => !typeRuleIds.includes(id));
                return [...filteredPrev, ...typeRuleIds];
            });
        }
    };

    const handleUnapplySelected = async () => {
        try {
            await onUnapplyRules(selectedRules);
            setSelectedRules([]);
            setIsSelectionMode(false);
            alert('선택한 규칙들이 성공적으로 적용 해제되었습니다.');
        } catch (error) {
            console.error('규칙 적용 해제 실패:', error);
            alert('규칙 적용 해제에 실패했습니다.');
        }
    };

    return (
        <div className="applied-rules-container">
            <div className="applied-rules-header">
                <h1>적용된 규칙</h1>
                <button 
                    className="selection-button"
                    onClick={() => setIsSelectionMode(!isSelectionMode)}
                >
                    {isSelectionMode ? '적용 해제' : '규칙 선택'}
                </button>
            </div>
            
            {/* 단순 응답 규칙 */}
            <div className="applied-rules-section">
                <h3>단순 응답 규칙 목록</h3>
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
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 조건부 응답 규칙 */}
            <div className="applied-rules-section">
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
                                                    <strong>{condition.condition}:</strong> {condition.response}
                                                </div>
                                            ))}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 적용 해제 버튼 */}
            {isSelectionMode && selectedRules.length > 0 && (
                <div className="unapply-rules-section">
                    <button 
                        className="unapply-rules-button"
                        onClick={handleUnapplySelected}
                    >
                        선택한 규칙 적용 해제하기 ({selectedRules.length}개)
                    </button>
                </div>
            )}
        </div>
    );
};

export default AppliedRules; 